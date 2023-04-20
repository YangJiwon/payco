package com.simple;

import com.was.model.HttpRequest;
import com.was.model.HttpResponse;

public interface SimpleServlet {
    void service(HttpRequest request, HttpResponse response);
}
