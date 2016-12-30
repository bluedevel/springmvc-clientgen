package com.bluedevel.smvcclientgen;

import java.util.List;

/**
 * @author Robin Engel
 */
public class ClientGeneratorConfiguration {

    private Class<?> controllerClass;
    private List<ClientGeneratorControllerDecleration> controllerDeclarations;

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<ClientGeneratorControllerDecleration> getControllerDeclarations() {
        return controllerDeclarations;
    }

    public void setControllerDeclarations(List<ClientGeneratorControllerDecleration> controllerDeclarations) {
        this.controllerDeclarations = controllerDeclarations;
    }
}
