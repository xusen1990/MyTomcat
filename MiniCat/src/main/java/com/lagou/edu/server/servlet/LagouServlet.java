package com.lagou.edu.server.servlet;

import com.lagou.edu.server.request.Request;
import com.lagou.edu.server.response.Response;
import com.lagou.edu.server.servlet.HttpServlet;
import com.lagou.edu.server.utils.HttpProtocolUtil;

import java.io.IOException;

public class LagouServlet extends HttpServlet {
    @Override
    public void doGet(Request request, Response response) {

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String context = "<h1>LagouServlet get</h1>";
        try {
            response.output(HttpProtocolUtil.getHttpHeader200(context.getBytes().length) + context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(Request request, Response response) {
        String context = "<h1>LagouServlet post</h1>";
        try {
            response.output(HttpProtocolUtil.getHttpHeader200(context.getBytes().length) + context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }
}
