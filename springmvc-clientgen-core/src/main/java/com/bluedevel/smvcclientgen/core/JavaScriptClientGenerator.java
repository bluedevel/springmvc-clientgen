package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDeclaration;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Robin Engel
 */
public class JavaScriptClientGenerator implements ClientGenerator {

    private Logger log = Logger.getLogger(JavaScriptClientGenerator.class.getName());

    private String templateName;

    public JavaScriptClientGenerator() {
        this("javascript");
    }

    public JavaScriptClientGenerator(String templateName) {
        this.templateName = templateName;
    }

    public String render(ClientGeneratorConfiguration config) throws Exception {

        if (config.getControllerDeclarations() == null) {
            return "";
        }

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        Template template = ve.getTemplate("templates/" + templateName + ".vm", "UTF-8");

        List<FunctionConfig> functions = config.getControllerDeclarations().stream()
                .flatMap(this::getDummies)
                .map(this::getFunctionConfig)
                .collect(Collectors.toList());

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

    private Stream<ClientGeneratorControllerDeclarationDummy> getDummies(ClientGeneratorControllerDeclaration declaration) {
        return Arrays.stream(declaration.getMethods())
                .map(e -> this.getDummy(declaration, e));
    }

    private ClientGeneratorControllerDeclarationDummy getDummy(ClientGeneratorControllerDeclaration declaration, RequestMethod requestMethod) {
        ClientGeneratorControllerDeclarationDummy dummy = new ClientGeneratorControllerDeclarationDummy();
        try {
            BeanUtils.copyProperties(dummy, declaration);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        dummy.method = requestMethod;
        return dummy;
    }

    private FunctionConfig getFunctionConfig(ClientGeneratorControllerDeclarationDummy declaration) {
        String methodName = declaration.getControllerMethod().getName();
        RequestMethod requestMethod = declaration.method;

        // check weather implementing method name starts with http method
        if (!methodName.toLowerCase().startsWith(requestMethod.name().toLowerCase())) {
            methodName = requestMethod.name().toLowerCase() + capitalizeFirstLetter(methodName);
        }

        FunctionConfig function = new FunctionConfig();
        function.name = methodName;
        function.method = requestMethod.name();
        function.url = declaration.getPath();
        function.consumes = declaration.getConsumes();
        function.produces = declaration.getProduces();

        return function;
    }

    private String capitalizeFirstLetter(String str) {
        if (str.length() == 0) {
            return "";
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static class ClientGeneratorControllerDeclarationDummy extends ClientGeneratorControllerDeclaration {
        private RequestMethod method;
    }

    public static class FunctionConfig {
        private String name;
        private String method;
        private String url;
        private String consumes;
        private String produces;

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

        public String getProduces() {
            return produces;
        }
    }
}
