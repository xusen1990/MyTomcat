package com.lagou.edu.server.mapper;

import com.lagou.edu.server.servlet.Servlet;

public class MappedWrapper {

    private String name;
    private Servlet servlet;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
}
