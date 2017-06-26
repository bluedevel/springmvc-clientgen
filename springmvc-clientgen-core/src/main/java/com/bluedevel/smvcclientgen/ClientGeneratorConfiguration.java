package com.bluedevel.smvcclientgen;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a controller containing multiple handlers.
 *
 * @author Robin Engel
 */
public class ClientGeneratorConfiguration {

    private String name;
    private URL baseURL;
    private Class<?> controllerClass;
    private List<ResourceHandler> resourceHandlers;

    public ClientGeneratorConfiguration() {
        resourceHandlers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<ResourceHandler> getResourceHandlers() {
        return resourceHandlers;
    }

    public void setResourceHandlers(List<ResourceHandler> resourceHandlers) {
        this.resourceHandlers = resourceHandlers;
    }
}
