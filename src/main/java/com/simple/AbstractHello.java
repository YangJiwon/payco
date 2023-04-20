package com.simple;

import java.util.Map;
import java.util.StringJoiner;

import org.json.simple.JSONObject;

import com.was.common.CommonConstants;
import com.was.model.HttpRequest;
import com.was.model.HttpResponse;

public abstract class AbstractHello implements SimpleServlet{
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		StringJoiner msg = new StringJoiner(CommonConstants.GAP);
		msg.add(getMsg());

		addQueryString(msg, request.getQuery());
		JSONObject body = request.getBody();
		if(CommonConstants.POST.equals(request.getMethod()) && null != body){
			addBody(msg, body);
		}

		response.sendHeader( CommonConstants.OK,  msg.toString());
	}

	protected abstract String getMsg();

	private void addQueryString(StringJoiner msg, Map<String, String> map){
		for(String key : map.keySet()){
			msg.add(map.get(key));
		}
	}

	private void addBody(StringJoiner msg, JSONObject body){
		for(Object key : body.keySet()){
			Object v = body.get(key);
			msg.add(v.toString());
		}
	}
}
