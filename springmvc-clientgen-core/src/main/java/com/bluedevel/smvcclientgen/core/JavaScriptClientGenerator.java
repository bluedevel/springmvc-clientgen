package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDeclaration;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Engel
 */
public class JavaScriptClientGenerator implements ClientGenerator {

    public String render(ClientGeneratorConfiguration config) throws Exception {
        if (config.getControllerDeclarations() == null) {
            return "";
        }

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        Template template = ve.getTemplate("templates/javascript.vm", "UTF-8");

        List<FunctionConfig> functions = new ArrayList<>();
        for (ClientGeneratorControllerDeclaration declaration : config.getControllerDeclarations()) {
            if (declaration.getPath() == null || declaration.getPath().length == 0) {
                continue;
            }

            if (declaration.getMethod() == null || declaration.getMethod().length == 0) {
                declaration.setMethod(new RequestMethod[]{RequestMethod.GET});
            }

            String consumes = null;
            if (declaration.getConsumes() != null && declaration.getConsumes().length > 0) {
                consumes = declaration.getConsumes()[0];
            }

            for (RequestMethod requestMethod : declaration.getMethod()) {
                String methodName = declaration.getControllerMethod().getName();

                // check weather implementing method name starts with http method
                if (!methodName.toLowerCase().startsWith(requestMethod.name().toLowerCase())) {
                    methodName = requestMethod.name().toLowerCase() + capitalizeFirstLetter(methodName);
                }

                FunctionConfig function = new FunctionConfig();
                function.name = methodName;
                function.method = requestMethod.name();
                function.url = config.getBaseURL().toString() + declaration.getPath()[0];
                function.consumes = consumes;

                functions.add(function);
            }
        }

        String className = config.getName();
        String baseUrl = config.getBaseURL() != null ? config.getBaseURL().toString() : "";

        VelocityContext context = new VelocityContext();
        context.put("className", className);
        context.put("baseUrl", baseUrl);
        context.put("functions", functions);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    private String capitalizeFirstLetter(String str) {
        if (str.length() == 0) {
            return "";
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static class FunctionConfig {
        private String name;
        private String method;
        private String url;
        private String consumes;

        public String getName() {
            return name;
        }

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public String getConsumes() {
            return consumes;
        }
    }
}
