package com.lagou.edu.server.mapper;

import java.util.HashMap;
import java.util.Map;

public class MappedHost {

    private String name;
    private Map<String, MappedContext> mappedContextMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, MappedContext> getMappedContextMap() {
        return mappedContextMap;
    }

    public void setMappedContextMap(Map<String, MappedContext> mappedContextMap) {
        this.mappedContextMap = mappedContextMap;
    }
}
