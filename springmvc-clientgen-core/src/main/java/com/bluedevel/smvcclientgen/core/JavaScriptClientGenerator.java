package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDecleration;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Robin Engel
 */
public class JavaScriptClientGenerator implements ClientGenerator {

    public String render(ClientGeneratorConfiguration config) throws Exception {
        if (config.getControllerDeclarations() == null) {
            return "";
        }

        String className = config.getControllerClass().getSimpleName();

        StringBuilder source = new StringBuilder();
        source.append("var ")
                .append(className)
                .append("=")
                .append(className).append(" || {};");

        for (ClientGeneratorControllerDecleration decleration : config.getControllerDeclarations()) {
            if (decleration.getPath() == null || decleration.getPath().length == 0) {
                continue;
            }

            if (decleration.getMethod() == null || decleration.getMethod().length == 0) {
                decleration.setMethod(new RequestMethod[]{RequestMethod.GET});
            }

            for (RequestMethod requestMethod : decleration.getMethod()) {
                String methodName = decleration.getControllerMethod().getName();

                // check weather implementing method name starts with http method
                if (!methodName.toLowerCase().startsWith(requestMethod.name().toLowerCase())) {
                    methodName = requestMethod.name().toLowerCase() + capitalizeFirstLetter(methodName);
                }

                source.append(className).append(".").append(methodName).append("=function(onLoad){")
                        .append("var request = new XMLHttpRequest();")
                        .append("request.open(")
                        .append("'").append(requestMethod.name()).append("'")
                        .append(",")
                        .append("'").append(decleration.getPath()[0]).append("'").append(");");

                source.append("request.addEventListener('load', onLoad);");
                source.append("request.send();");

                //close function
                source.append("};");
            }
        }

        return source.toString();
    }

    private String capitalizeFirstLetter(String str) {
        if (str.length() == 0) {
            return "";
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
