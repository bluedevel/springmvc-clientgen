package com.bluedevel.smvcclientgen;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Engel
 */
public class ClientGeneratorConfiguration {

    private String name;
    private Class<?> controllerClass;
    private List<ClientGeneratorControllerDeclaration> controllerDeclarations;

    public ClientGeneratorConfiguration() {
        controllerDeclarations = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<ClientGeneratorControllerDeclaration> getControllerDeclarations() {
        return controllerDeclarations;
    }

    public void setControllerDeclarations(List<ClientGeneratorControllerDeclaration> controllerDeclarations) {
        this.controllerDeclarations = controllerDeclarations;
    }
}
