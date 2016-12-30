package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

/**
 * @author Robin Engel
 */
public class Generator {

    private String name;
    private String implementation;
    private String fileEnding;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public void setFileEnding(String fileEnding) {
        this.fileEnding = fileEnding;
    }
}
