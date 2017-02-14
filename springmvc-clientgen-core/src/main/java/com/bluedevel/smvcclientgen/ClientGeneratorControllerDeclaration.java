package com.bluedevel.smvcclientgen;

import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

/**
 * @author Robin Engel
 */
public class ClientGeneratorControllerDeclaration {

    private Method controllerMethod;
    private String name;
    private String path;
    private RequestMethod[] methods;
    private String[] params;
    private String[] headers;
    private String consumes;
    private String produces;

    public ClientGeneratorControllerDeclaration() {
    }

    public ClientGeneratorControllerDeclaration(Method controllerMethod, String name, String path, RequestMethod[] methods, String[] params, String[] headers, String consumes, String produces) {
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

    public RequestMethod[] getMethods() {
        return methods;
    }

    public void setMethods(RequestMethod[] methods) {
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

}
