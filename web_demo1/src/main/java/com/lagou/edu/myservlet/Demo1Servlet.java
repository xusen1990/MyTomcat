package com.lagou.edu.myservlet;

import com.lagou.edu.server.request.Request;
import com.lagou.edu.server.response.Response;
import com.lagou.edu.server.servlet.HttpServlet;
import com.lagou.edu.server.utils.HttpProtocolUtil;

import java.io.IOException;

public class Demo1Servlet extends HttpServlet {
    @Override
    public void doGet(Request request, Response response) {
        doPost(request,response);
    }

    @Override
    public void doPost(Request request, Response response) {
        String context = "<h1>web_demo1 servlet</h1>";
        try {
            response.output(HttpProtocolUtil.getHttpHeader200(context.getBytes().length) + context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        super.service(request, response);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
