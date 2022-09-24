package com.lagou.edu.server.request;

import com.lagou.edu.server.mapper.MappedContext;
import com.lagou.edu.server.mapper.MappedHost;
import com.lagou.edu.server.mapper.MappedWrapper;
import com.lagou.edu.server.servlet.HttpServlet;
import com.lagou.edu.server.response.Response;
import com.lagou.edu.server.servlet.Servlet;

import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

public class RequestProcessor extends Thread {

    private Socket socket;
    private Map<String, MappedHost> servletMap;

    public RequestProcessor(Socket socket, Map<String, MappedHost> servletMap) {
        this.socket = socket;
        this.servletMap = servletMap;
    }

    @Override
    public void run() {
        try{
            // 使用BIO模式
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Resposen对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            // 静态资源处理
            if(getHttpServlet(request.getUrl()) == null){
                response.outputHtml(request.getUrl());
            }else {
                // 动态资源处理
                Servlet httpServlet = getHttpServlet(request.getUrl());
                httpServlet.service(request,response);
            }
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Servlet getHttpServlet(String url){
        String[] splits = url.split("/");
        String host = splits[0];
        String context = splits[1];
        String wrapper = splits[2];

        MappedHost mappedHost = this.servletMap.get(host);

        if(mappedHost == null) return null;

        Map<String, MappedContext> mappedContextMap = mappedHost.getMappedContextMap();

        MappedContext mappedContext = mappedContextMap.get(context);

        if(mappedContext == null) return null;

        Map<String, MappedWrapper> mappedWrapperMap = mappedContext.getMappedWrapperMap();

        MappedWrapper mappedWrapper = mappedWrapperMap.get(wrapper);

        if(mappedWrapper == null) return null;

        return  mappedWrapper.getServlet();


    }
}
