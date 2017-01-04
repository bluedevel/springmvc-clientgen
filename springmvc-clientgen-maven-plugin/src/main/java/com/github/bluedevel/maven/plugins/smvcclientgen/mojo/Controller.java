package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import java.net.URL;

/**
 * @author Robin Engel
 */
public class Controller {

    private String implementation;
    private String generator;
    private URL baseUrl;

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }
}
