package com.lagou.edu.server.servlet;

import com.lagou.edu.server.request.Request;
import com.lagou.edu.server.response.Response;

public interface Servlet {

    void init() throws Exception;

    void destroy() throws Exception;

    void service(Request request, Response response) throws Exception;
}
