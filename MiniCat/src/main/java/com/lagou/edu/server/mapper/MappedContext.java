package com.lagou.edu.server.mapper;

import java.util.HashMap;
import java.util.Map;

public class MappedContext {

    private String name;
    private Map<String, MappedWrapper> mappedWrapperMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, MappedWrapper> getMappedWrapperMap() {
        return mappedWrapperMap;
    }

    public void setMappedWrapperMap(Map<String, MappedWrapper> mappedWrapperMap) {
        this.mappedWrapperMap = mappedWrapperMap;
    }
}
