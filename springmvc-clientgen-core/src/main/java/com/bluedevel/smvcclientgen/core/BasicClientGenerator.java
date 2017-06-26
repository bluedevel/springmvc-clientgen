package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ResourceHandler;
import com.bluedevel.smvcclientgen.Parameter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

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
public class BasicClientGenerator implements ClientGenerator {

    private Logger log = Logger.getLogger(BasicClientGenerator.class.getName());

    private String templateName;

    public BasicClientGenerator(String templateName) {
        this.templateName = templateName;
    }

    public String render(ClientGeneratorConfiguration config) throws Exception {

        if (config.getResourceHandlers() == null) {
            return "";
        }

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        Template template = ve.getTemplate("templates/" + templateName + ".vm", "UTF-8");

        List<FunctionConfig> functions = config.getResourceHandlers().stream()
                .flatMap(this::forEachRequestMethod)
                .map(this::getFunctionConfig)
                .collect(Collectors.toList());

        String className = config.getName();
        String baseUrl = config.getBaseURL() != null ? config.getBaseURL().toString() : "";

        VelocityContext context = new VelocityContext();
        context.put("className", className);
        context.put("baseUrl", baseUrl);
        context.put("functions", functions);

        context.put("typePath", Parameter.Type.PATH);
        context.put("typeQuery", Parameter.Type.QUERY);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    private Stream<EnhancedResourceHandler> forEachRequestMethod(ResourceHandler declaration) {
        return Arrays.stream(declaration.getMethods())
                .map(e -> this.getEnhancedControllerDecleration(declaration, e));
    }

    private EnhancedResourceHandler getEnhancedControllerDecleration(ResourceHandler declaration, String requestMethod) {
        EnhancedResourceHandler dummy = new EnhancedResourceHandler();
        try {
            BeanUtils.copyProperties(dummy, declaration);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        dummy.method = requestMethod;
        return dummy;
    }

    private FunctionConfig getFunctionConfig(EnhancedResourceHandler declaration) {
        String methodName = declaration.getControllerMethod().getName();
        String requestMethod = declaration.method;

        // check weather implementing method name starts with http method
        if (!methodName.toLowerCase().startsWith(requestMethod.toLowerCase())) {
            methodName = requestMethod.toLowerCase() + capitalizeFirstLetter(methodName);
        }

        FunctionConfig function = new FunctionConfig();
        function.setName(methodName);
        function.setMethod(requestMethod);
        function.setPath(declaration.getPath());
        function.setConsumes(declaration.getConsumes());
        function.setProduces(declaration.getProduces());
        function.setParameters(declaration.getParameters());

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

    private static class EnhancedResourceHandler extends ResourceHandler {
        private String method;
    }
}
