package com.bluedevel.smvcclientgen;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;

/**
 * @author Robin Engel
 */
public class ClientGeneratorConfiguration {

    private String name;
    private String[] path;
    private RequestMethod[] method;
    private String[] params;
    private String[] headers;
    private String[] consumes;
    private String[] produces;

    public ClientGeneratorConfiguration() {
    }

    public ClientGeneratorConfiguration(String name, String[] path, RequestMethod[] method, String[] params, String[] headers, String[] consumes, String[] produces) {
        this.name = name;
        this.path = path;
        this.method = method;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public RequestMethod[] getMethod() {
        return method;
    }

    public void setMethod(RequestMethod[] method) {
        this.method = method;
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

    public String[] getConsumes() {
        return consumes;
    }

    public void setConsumes(String[] consumes) {
        this.consumes = consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    public void setProduces(String[] produces) {
        this.produces = produces;
    }

    @Override
    public String toString() {
        return "ClientGeneratorConfiguration{" +
                "name='" + name + '\'' +
                ", path=" + Arrays.toString(path) +
                ", method=" + Arrays.toString(method) +
                ", params=" + Arrays.toString(params) +
                ", headers=" + Arrays.toString(headers) +
                ", consumes=" + Arrays.toString(consumes) +
                ", produces=" + Arrays.toString(produces) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientGeneratorConfiguration)) return false;

        ClientGeneratorConfiguration that = (ClientGeneratorConfiguration) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getPath(), that.getPath())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getMethod(), that.getMethod())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getParams(), that.getParams())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getHeaders(), that.getHeaders())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getConsumes(), that.getConsumes())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getProduces(), that.getProduces());
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + Arrays.hashCode(getPath());
        result = 31 * result + Arrays.hashCode(getMethod());
        result = 31 * result + Arrays.hashCode(getParams());
        result = 31 * result + Arrays.hashCode(getHeaders());
        result = 31 * result + Arrays.hashCode(getConsumes());
        result = 31 * result + Arrays.hashCode(getProduces());
        return result;
    }
}
