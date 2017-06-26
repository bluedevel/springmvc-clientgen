package com.bluedevel.smvcclientgen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a single resource handler.
 *
 * @author Robin Engel
 */
public class ResourceHandler {

    private Method controllerMethod;
    private String name;
    private String path;
    private String[] methods;
    private String[] params;
    private String[] headers;
    private String consumes;
    private String produces;
    private List<Parameter> parameters;

    public ResourceHandler() {
        this.parameters = new ArrayList<>();
    }

    public ResourceHandler(Method controllerMethod, String name, String path, String[] methods, String[] params, String[] headers, String consumes, String produces) {
        this.controllerMethod = controllerMethod;
        this.name = name;
        this.path = path;
        this.methods = methods;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    public Method getControllerMethod() {
        return controllerMethod;
    }

    public void setControllerMethod(Method controllerMethod) {
        this.controllerMethod = controllerMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getMethods() {
        return methods;
    }

    public void setMethods(String[] methods) {
        this.methods = methods;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String getConsumes() {
        return consumes;
    }

    public void setConsumes(String consumes) {
        this.consumes = consumes;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}
