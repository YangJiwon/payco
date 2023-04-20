package com.was.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

@Getter
public class HttpResponse {
	private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
	private final OutputStream outputStream;
	private final String version;

	public HttpResponse(OutputStream outputStream, String version) {
		this.outputStream = outputStream;
		this.version = version;
	}

	public void sendHeader(String responseCode, String msg){
		this.sendHeader("", responseCode, msg);
	}

	public void sendHeader(String filePath, String responseCode, String msg) {
		if (!this.version.startsWith("HTTP/")) {
			return;
		}

		try {
			byte[] data = getData(filePath);

			Writer writer = new OutputStreamWriter(outputStream);
			writer.write("HTTP/1.0 " + responseCode + "\r\n");
			writer.write("Date: " + new Date() + "\r\n");
			writer.write("Server: JHTTP 2.0\r\n");
			writer.write("Content-type: " + "text/html; charset=utf-8" + "\r\n\r\n");
			if(null != msg && !"".equals(msg)){
				writer.write(msg);
			}

			writer.flush();
			outputStream.write(data);
			outputStream.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public byte[] getData(File file) {
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public byte[] getData(String filePath){
		if("".equals(filePath)){
			return new byte[0];
		}

		File file = getFile(filePath);
		return getData(file);
	}

	public File getFile(String filePath){
		return new File(filePath);
	}
}
