package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDeclaration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * @author Robin Engel
 */
@Mojo(name = "generate-clients")
public class SpringMVCClientGenMojo extends AbstractMojo {

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    /**
     * All controllers to generate clients for.
     */
    @Parameter
    private Controller[] controllers;

    /**
     * A list of custom generators.
     * TODO: implement this feature
     */
    @Parameter
    private Generator[] generators;

    /**
     * The generator to use by default for all controllers.
     */
    @Parameter(defaultValue = "javascript")
    private String generator;

    /**
     * The target file or directory to write the client(s) to.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/clients")
    private File target;

    /**
     * A common base url to use for all clients.
     */
    @Parameter
    private URL baseUrl;

    private ClientGeneratorFactory generatorFactory;

    // TODO extract this stuff to another class?
    private ClassLoader classLoader;

    public SpringMVCClientGenMojo() {
        generatorFactory = new ClientGeneratorFactory();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (controllers == null || controllers.length == 0) {
            return;
        }

        classLoader = new URLClassLoader(
                new URL[]{getOutputUrl()},
                this.getClass().getClassLoader());

        configureGeneratorFactory();

        ClientGeneratorRenderer renderer = new ClientGeneratorRenderer(target, getLog());

        try {
            processControllers(renderer);
        } catch (RuntimeException e) {
            StreamExceptions.handle(e, MojoExecutionException.class);
            StreamExceptions.handle(e, MojoFailureException.class);
            throw e;
        }
    }

    private void processControllers(ClientGeneratorRenderer renderer) {
        stream(controllers)
                .map(this::toEnhancedConfiguration)
                .peek(this::fillControllerClass)
                .filter(this::isSpringMvcController)
                .peek(this::fillControllerName)
                .peek(this::fillBaseUrl)
                .peek(this::fillGenerator)
                .peek(this::fillDeclaration)
                .forEach(renderer::render);

        /* TODO
            Fiddle this warning in there

            getLog().warn("A single file is specified to write clients to but multiple " +
                        "controllers are found. " +
                        "All generated clients will be overwritten by the next one!");
         */
    }

    /**
     * Get the output directory of the {@link MavenProject} as a {@link URL}
     */
    private URL getOutputUrl() throws MojoExecutionException {
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        try {
            return outputDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Failed to find output folder", e);
        }
    }

    /**
     * All client generators are configured here.
     * Custom generators are not supported yet.
     */
    private void configureGeneratorFactory() throws MojoFailureException {
        generatorFactory.reset();
        generatorFactory.registerDefaultGenerators();

        try {
            generatorFactory.registerGenerators(generators);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new MojoFailureException("Couldn't register generators", e);
        }
    }

    /**
     * Get a {@link EnhancedClientGenConfig} for a {@link Controller} for further processing in a stream.
     */
    private EnhancedClientGenConfig toEnhancedConfiguration(Controller controller) {
        EnhancedClientGenConfig config = new EnhancedClientGenConfig();
        config.setName(controller.getName());
        config.setBaseURL(controller.getBaseUrl());

        // set controller here to fetch the data later on
        config.setController(controller);
        return config;
    }

    /**
     * Enhance a {@link EnhancedClientGenConfig} with it's controllers {@link Class}.<br>
     * The configured {@link ClassLoader} will try to load the {@link Class} specified in the {@link Controller}.
     */
    private void fillControllerClass(EnhancedClientGenConfig config) {
        try {
            Class<?> clazz = classLoader.loadClass(config.getController().getImplementation());
            config.setControllerClass(clazz);
        } catch (ClassNotFoundException e) {
            StreamExceptions.throwSilent(new MojoFailureException(
                    "Could not scan class", e));
        }
    }

    /**
     * Evaluate weather the controller class of a {@link ClientGeneratorConfiguration} is a proper spring-mvc controller.
     */
    private boolean isSpringMvcController(ClientGeneratorConfiguration config) {
        Class<?> clazz = config.getControllerClass();
        if (clazz.isAnnotationPresent(org.springframework.stereotype.Controller.class)
                || clazz.isAnnotationPresent(RestController.class)) {
            getLog().warn("Class " + clazz.getName() + " is not an actual controller! " +
                    "Class must be annotated with " + org.springframework.stereotype.Controller.class.getName() +
                    " or " + RestController.class.getName());
            return true;
        }

        return false;
    }

    /**
     * Ensures that a proper name is set for a {@link ClientGeneratorConfiguration}.
     * If it's not, a default will be generated.
     */
    private void fillControllerName(ClientGeneratorConfiguration config) {
        Class<?> controllerClass = config.getControllerClass();
        String name = StringUtils.defaultIfEmpty(
                config.getName(), getControllerName(controllerClass));
        config.setName(name);
    }

    /**
     * Generates a controller name from a {@link Class}.
     */
    private String getControllerName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        name = name.replace("Controller", "");
        name = name.replace("Resource", "");
        name = name + "Client";
        return name;
    }

    /**
     * Sets the base url of a {@link ClientGeneratorConfiguration} to the default one if none is configured.
     */
    private void fillBaseUrl(ClientGeneratorConfiguration config) {
        config.setBaseURL(ObjectUtils.defaultIfNull(config.getBaseURL(), baseUrl));
    }

    /**
     * Enhances a {@link EnhancedClientGenConfig} with a generator,
     * which is loaded from the configured {@link ClientGeneratorFactory}.
     */
    private void fillGenerator(EnhancedClientGenConfig config) {
        String generatorName = StringUtils.defaultIfEmpty(
                config.getController().getGenerator(), generator);
        config.setGenerator(generatorFactory.getClientGenerator(generatorName));
    }

    /**
     * Enhance a {@link EnhancedClientGenConfig} with {@link ClientGeneratorControllerDeclaration}s.<br>
     * A declaration is created for every {@link RequestMethod}
     * on the {@link RequestMapping} of this {@link ClientGeneratorConfiguration}
     */
    private void fillDeclaration(ClientGeneratorConfiguration config) {
        List<ClientGeneratorControllerDeclaration> declarations = new ArrayList<>();
        for (Method method : config.getControllerClass().getMethods()) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

            if (requestMapping == null) {
                continue;
            }

            if (requestMapping.consumes().length > 1) {
                getLog().warn("Multiple consumes are not supported! " +
                        "The first one will be used on the client");
            }

            ClientGeneratorControllerDeclaration decleration = new ClientGeneratorControllerDeclaration();
            decleration.setControllerMethod(method);
            decleration.setName(requestMapping.name());
            decleration.setPath(
                    getPath(requestMapping));
            decleration.setMethods(
                    getMethods(requestMapping));
            decleration.setHeaders(requestMapping.headers());
            decleration.setParams(requestMapping.params());
            decleration.setConsumes(
                    getConsumes(requestMapping));
            decleration.setProduces(
                    getProduces(requestMapping));
            fillParameters(decleration);

            declarations.add(decleration);
        }
        config.setControllerDeclarations(declarations);
    }

    private String[] getMethods(RequestMapping mapping) {
        List<String> methods = Arrays.stream(mapping.method())
                .map(RequestMethod::name)
                .collect(Collectors.toList());

        if (methods.size() == 0) {
            getLog().warn("No method is configured in mapping " + mapping + "! " +
                    "GET will be used as default.");
            methods = Collections.singletonList(RequestMethod.GET.name());
        }

        return methods.toArray(new String[0]);
    }

    private String getPath(RequestMapping mapping) {
        String[] path = mapping.path().length > 0 ?
                mapping.path() : mapping.value();

        if (path.length > 0) {
            if (path.length > 1) {
                getLog().warn("Multiple paths are not supported! " +
                        "The first one will be used on the client");
            }
            return path[0];
        }

        getLog().warn("No path is configured for client!");
        return "";
    }

    private String getConsumes(RequestMapping mapping) {
        String[] consumes = mapping.consumes();

        if (consumes.length > 0) {
            if (consumes.length > 1) {
                getLog().warn("Multiple consumes are not supported! " +
                        "The first one will be used on the client");
            }
            return consumes[0];
        }

        return null;
    }

    private String getProduces(RequestMapping mapping) {
        String[] produces = mapping.consumes();

        if (produces.length > 0) {
            if (produces.length > 1) {
                getLog().warn("Multiple consumes are not supported! " +
                        "The first one will be used on the client");
            }
            return produces[0];
        }

        return null;
    }

    /**
     * Scans the parameters of each controller method to find path and query parameters.
     * Those are put into the decleration for the generator to render.
     *
     * @param decleration {@link ClientGeneratorControllerDeclaration} to scan for parameters
     */
    private void fillParameters(ClientGeneratorControllerDeclaration decleration) {
        for (java.lang.reflect.Parameter methodParameter : decleration.getControllerMethod().getParameters()) {
            RequestParam requestParam = methodParameter.getAnnotation(RequestParam.class);
            PathVariable pathVariable = methodParameter.getAnnotation(PathVariable.class);

            com.bluedevel.smvcclientgen.Parameter parameter = null;
            if (pathVariable != null) {
                String name = org.apache.commons.lang.StringUtils.defaultIfEmpty(
                        pathVariable.name(), pathVariable.value());

                if (decleration.getPath().contains("{" + name + "}")) {
                    parameter = new com.bluedevel.smvcclientgen.Parameter(name, com.bluedevel.smvcclientgen.Parameter.Type.PATH);
                }
            } else if (requestParam != null) {
                String name = org.apache.commons.lang.StringUtils.defaultIfEmpty(
                        requestParam.name(), requestParam.value());

                parameter = new com.bluedevel.smvcclientgen.Parameter(name, com.bluedevel.smvcclientgen.Parameter.Type.QUERY);
            } else {
                continue;
            }

            decleration.getParameters().add(parameter);
        }
    }
}
