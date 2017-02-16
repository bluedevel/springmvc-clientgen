package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDeclaration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

/**
 * @author Robin Engel
 */
@Mojo(name = "generate-clients")
public class SpringMVCClientGenMojo extends AbstractMojo {

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    @Parameter
    private Controller[] controllers;

    @Parameter
    private Generator[] generators;

    @Parameter
    private String generator;

    @Parameter
    private File target;

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

        try {
            processControllers();
        } catch (RuntimeException e) {
            handleSilentException(e);
        }
    }

    private void processControllers() {
        stream(controllers)
                .map(this::getConfiguration)
                .peek(this::fillControllerClass)
                .filter(this::isSpringMvcController)
                .peek(this::fillControllerName)
                .peek(this::fillBaseUrl)
                .peek(this::fillGenerator)
                .peek(this::fillDeclaration)
                .forEach(this::render);

        /* TODO
            Fiddle this warning in there

            getLog().warn("A single file is specified to write clients to but multiple " +
                        "controllers are found. " +
                        "All generated clients will be overwritten by the next one!");
         */
    }

    /**
     * Wrap a exception inside a {@link RuntimeException} so that it can be thrown out of a stream
     * as an unchecked exception.
     */
    private void throwSilent(AbstractMojoExecutionException e) {
        throw new RuntimeException("This exception serves only as a wrapper. You shouldn't ever see it!", e);
    }

    /**
     * Take a {@link RuntimeException} and unwrap the cause if it's one of the expected exceptions.<br>
     * This is used for catching expected exceptions out of a stream, which are wrapped in an unexpected exception.
     */
    private void handleSilentException(RuntimeException e) throws MojoExecutionException, MojoFailureException {
        Throwable cause = e.getCause();

        if (cause instanceof MojoExecutionException) {
            throw (MojoExecutionException) cause;
        } else if (cause instanceof MojoFailureException) {
            throw (MojoFailureException) cause;
        }

        throw e;
    }

    /**
     * Enhances {@link ClientGeneratorConfiguration} with data needed inside the controller processing stream.
     */
    private static class EnhancedClientGenConfig extends ClientGeneratorConfiguration {
        private Controller controller;
        private ClientGeneratorFactory.ClientGenerator generator;
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
     * TODO
     */
    private EnhancedClientGenConfig getConfiguration(Controller controller) {
        EnhancedClientGenConfig config = new EnhancedClientGenConfig();
        config.setName(controller.getName());
        config.setBaseURL(controller.getBaseUrl());

        // set controller here to fetch the data later on
        config.controller = controller;
        return config;
    }

    /**
     * Enhance a {@link EnhancedClientGenConfig} with it's controllers {@link Class}.<br>
     * The configured {@link ClassLoader} will try to load the {@link Class} specified in the {@link Controller}.
     */
    private void fillControllerClass(EnhancedClientGenConfig config) {
        try {
            Class<?> clazz = classLoader.loadClass(config.controller.getImplementation());
            config.setControllerClass(clazz);
        } catch (ClassNotFoundException e) {
            throwSilent(new MojoFailureException(
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
                config.controller.getGenerator(), generator);
        config.generator = generatorFactory.getClientGenerator(generatorName);
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
            declarations.add(decleration);
        }
        config.setControllerDeclarations(declarations);
    }

    private RequestMethod[] getMethods(RequestMapping mapping) {
        RequestMethod[] methods = mapping.method();

        if (methods.length == 0) {
            getLog().warn("No method is configured in mapping " + mapping + "! " +
                    "GET will be used as default.");
            return new RequestMethod[]{RequestMethod.GET};
        }

        return methods;
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
     * Renders and writes a {@link EnhancedClientGenConfig} to the file or directory
     * configured by the plugin settings.
     */
    private void render(EnhancedClientGenConfig config) {
        boolean isFile = target.isFile() || target.getName().contains(".");

        ClientGeneratorFactory.ClientGenerator generator = config.generator;
        String source = callClientGenerator(generator, config);

        File file;
        if (isFile) {
            file = target;
            getLog().warn("A single file is specified to write clients to. " +
                    "If multiple clients are configured, they will be overwritten by one another!");
        } else {
            file = new File(target.getAbsolutePath() +
                    File.separator +
                    config.getName() +
                    "." +
                    generator.getFileEnding());
        }

        writeClient(file, source);
    }

    /**
     * Wraps the call of a {@link ClientGenerator}.
     */
    private String callClientGenerator(ClientGenerator clientGenerator, ClientGeneratorConfiguration config) {
        try {
            return clientGenerator.render(config);
        } catch (Exception e) {
            throwSilent(new MojoFailureException(
                    "Failed to render clients: " + e.getMessage(), e));
            // never reached, but a hack for exception handling with the stream api
            return null;
        }
    }

    /**
     * Writes a {@link String} to a {@link File} and creates the parent directory if necessary.
     */
    private void writeClient(File file, String source) {
        try {
            FileUtils.forceMkdirParent(file);
        } catch (IOException e) {
            throwSilent(new MojoFailureException(
                    "Couldn't create parent directories for file " + file.getAbsolutePath(), e));
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            throwSilent(new MojoFailureException(
                    "Couldn't write client file to " + file.getAbsolutePath(), e));
        }

        PrintWriter printer;
        try {
            printer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            throwSilent(new MojoFailureException(
                    "File not found " + file.getAbsolutePath(), e));
            // never reached, but a hack for exception handling with the stream api
            return;
        }

        printer.print(source);
        printer.flush();
        printer.close();
    }
}
