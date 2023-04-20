package com.simple;

import java.time.LocalDateTime;

import com.was.common.CommonConstants;
import com.was.model.HttpRequest;
import com.was.model.HttpResponse;

public class Time implements SimpleServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        LocalDateTime dateTime = LocalDateTime.now();
        response.sendHeader(CommonConstants.OK, dateTime.format(CommonConstants.FORMATTER));
    }
}
