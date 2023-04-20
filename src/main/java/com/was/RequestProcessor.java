package com.was;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.simple.SimpleServlet;
import com.was.common.CommonConstants;
import com.was.common.CommonUtil;
import com.was.exception.ForbiddenException;
import com.was.exception.NotFoundException;
import com.was.model.HttpRequest;
import com.was.model.HttpResponse;
import com.was.model.Server;
import com.was.model.Settings;

public class RequestProcessor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);
	private final Socket connection;
	private final Settings settings;

	public RequestProcessor(Socket connection, Settings settings) {
		this.connection = connection;
		this.settings = settings;
	}

	@Override
	public void run() {
		HttpRequest request = getHttpRequest();
		OutputStream outputStream = getOutputStream();
		if(null == request || null == outputStream){
			throw new RuntimeException("올바르지 않은 요청입니다");
		}

		HttpResponse response = new HttpResponse(outputStream, request.getVersion());
		Server server = getServer(settings, request.getHost());

		try {
			String requestPath = request.getPath();
			isValidPath(requestPath, settings.getRestrictedExtension());
			String filePath = getFilePath(server, requestPath);

			File file = response.getFile(filePath);
			if (file.canRead()) {
				response.sendHeader(filePath, CommonConstants.OK, "");
				return;
			}

			Class<SimpleServlet> instance = getClass(request.getPath());
			if(null == instance){
				throw new NotFoundException("경로가 올바르지 않습니다");
			}

			SimpleServlet simpleServlet = instance.getDeclaredConstructor().newInstance();
			simpleServlet.service(request, response);
		} catch (ForbiddenException fe){
			logger.error(fe.getMessage(), fe);
			response.sendHeader(getRootPath(server, server.getForbidden()), CommonConstants.FORBIDDEN, fe.getMessage());
		} catch (NotFoundException nfe){
			logger.error(nfe.getMessage(), nfe);
			response.sendHeader(getRootPath(server, server.getNotFound()), CommonConstants.NOT_FOUND, nfe.getMessage());
		} catch (Exception ise){
			logger.error(ise.getMessage(), ise);
			response.sendHeader(getRootPath(server, server.getServerError()), CommonConstants.INTERNAL_SERVER_ERROR, ise.getMessage());
		} finally {
			try {
				connection.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private HttpRequest getHttpRequest(){
		try {
			return new HttpRequest(connection.getInputStream());
		} catch (IOException | ParseException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private BufferedOutputStream getOutputStream(){
		try {
			return new BufferedOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private Server getServer(Settings settings, String host){
		host = host.split(CommonConstants.COLON)[0];
		Map<String, Server> serverMap = settings.getServerMap();

		if(!serverMap.containsKey(host) || null == serverMap.get(host)) {
			throw new ForbiddenException("설정파일을 불러오지 못했습니다");
		}
		return serverMap.get(host);
	}

	private String getRootPath(Server server, String filePath){
		File path = new File("");
		return path.getAbsolutePath() + server.getRoot() + filePath;
	}

	private void isValidPath(String filePath, String restrictedExtension){
		if(filePath.contains(CommonConstants.POINT)) {
			String[] split = filePath.split(CommonConstants.POINT_DELIMITER);
			if(restrictedExtension.equals(split[1])){
				throw new ForbiddenException("제한된 확장자입니다");
			}
		}

		if (filePath.contains("../")) {
			throw new ForbiddenException("상위 디렉토리의 접근은 불가합니다");
		}
	}

	private String getFilePath(Server server, String filePath){
		if (filePath.endsWith(CommonConstants.SLASH)){
			filePath += server.getIndex();
		}

		if (filePath.endsWith("favicon.ico")){
			filePath = CommonConstants.SLASH + server.getIndex();
		}

		return getRootPath(server, filePath);
	}

	private Class<SimpleServlet> getClass(String path){
		Map<String, String> mappingMap = settings.getMappingMap();
		if(mappingMap.containsKey(path)){
			path = mappingMap.get(path);
		}

		String[] split = path.split(CommonConstants.POINT_DELIMITER);
		String className = getPackageName(split) + CommonConstants.POINT + getMethodName(split);

		try {
			return ((Class<SimpleServlet>) Class.forName(className));
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private String getPackageName(String[] path){
		String packageName = "com.simple";
		if(path.length <= 1){
			return packageName;
		}

		return packageName + CommonConstants.POINT + CommonUtil.removeDelimiter(path[0], CommonConstants.SLASH);
	}

	private String getMethodName(String[] path){
		String methodName = CommonUtil.removeDelimiter(path[0], CommonConstants.SLASH);
		if(path.length <= 1){
			return methodName;
		}

		return path[1];
	}
}
