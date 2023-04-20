package com.was.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.was.common.CommonConstants;

import lombok.Getter;

@Getter
public class HttpRequest {
	private final String method;
	private final String path;
	private final String version;
	private String host;
	private JSONObject body;
	private final Map<String, String> query = new HashMap<>();

	public HttpRequest(InputStream inputStream) throws IOException, ParseException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = bufferedReader.readLine();
		String[] first = line.split(CommonConstants.GAP);

		this.method = first[0];
		String[] path = first[1].split(CommonConstants.QUESTION_MARK_DELIMITER);
		this.path = path[0];
		this.version = first[2];

		while(null != (line = bufferedReader.readLine())){
			if("".equals(line)){
				break;
			}

			String[] headerArray = line.split(CommonConstants.GAP);
			String header = headerArray[0];
			if(header.startsWith("Host:")){
				this.host = headerArray[1].trim();
			}
		}

		setQueryString(path);
		setBody(bufferedReader);
	}

	private void setQueryString(String[] path){
		if(path.length <= 1){
			return;
		}

		String[] queryString = path[1].split(CommonConstants.AND_DELIMITER);
		for(String s: queryString) {
			String[] v = s.split(CommonConstants.EQUAL);
			query.put(v[0], v[1]);
		}
	}

	private void setBody(BufferedReader bufferedReader) throws IOException, ParseException {
		if(!CommonConstants.POST.equals(method)){
			return;
		}

		StringBuilder sb = new StringBuilder();
		while (bufferedReader.ready()) {
			sb.append((char) bufferedReader.read());
		}

		if (sb.length() == 0){
			return;
		}

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(sb.toString());
		this.body = (JSONObject) obj;
	}
}
