package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

/**
 * @author Robin Engel
 */
public class Controller {

    private String implementation;
    private String generator;

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
}
