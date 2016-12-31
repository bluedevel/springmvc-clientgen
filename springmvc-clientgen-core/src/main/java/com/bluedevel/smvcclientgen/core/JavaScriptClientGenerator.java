package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDecleration;

import java.util.Arrays;

/**
 * @author Robin Engel
 */
public class JavaScriptClientGenerator implements ClientGenerator {

    public String render(ClientGeneratorConfiguration config) {
        StringBuilder source = new StringBuilder();
        source.append("Class: ")
                .append(config.getControllerClass().getName())
                .append("\n");

        for (ClientGeneratorControllerDecleration decleration : config.getControllerDeclarations()) {
            source.append("Controller: ")
                    .append(decleration.getControllerMethod().getName())
                    .append(" Path: ")
                    .append(Arrays.toString(decleration.getPath()))
                    .append(" Method: ")
                    .append(Arrays.toString(decleration.getMethod()))
                    .append(" Produces: ")
                    .append(Arrays.toString(decleration.getProduces()))
                    .append("\n");
        }

        return source.toString();
    }

}
