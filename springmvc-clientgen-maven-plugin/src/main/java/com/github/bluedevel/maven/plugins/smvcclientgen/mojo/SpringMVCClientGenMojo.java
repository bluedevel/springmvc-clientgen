package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDeclaration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.web.bind.annotation.RequestMapping;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public SpringMVCClientGenMojo() {
        generatorFactory = new ClientGeneratorFactory();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (controllers == null || controllers.length == 0) {
            return;
        }

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{getOutputUrl()},
                this.getClass().getClassLoader());

        configureGeneratorFactory(classLoader);

        Map<String, Class<?>> controllerClasses = loadControllerClasses(classLoader);
        loadControllerNames(controllerClasses);
        Map<String, ClientGeneratorFactory.ClientGenerator> generators = loadGenerators();
        List<ClientGeneratorConfiguration> configurations = loadConfigurations(controllerClasses);

        for (ClientGeneratorConfiguration configuration : configurations) {
            loadDeclarations(configuration);
        }

        renderClients(configurations, generators);
    }

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
    private void configureGeneratorFactory(ClassLoader classLoader) throws MojoFailureException {
        generatorFactory.reset();
        generatorFactory.registerDefaultGenerators();

        try {
            generatorFactory.registerGenerators(generators);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new MojoFailureException("Couldn't register generators", e);
        }
    }

    /**
     * Scan over the controller configurations and generate a
     * default name if none is explicitly set.
     */
    private void loadControllerNames(Map<String, Class<?>> controllerClasses) {
        for (Controller controller : controllers) {
            Class<?> controllerClass = controllerClasses.get(controller.getImplementation());
            String name = StringUtils.defaultIfEmpty(
                    controller.getName(), getControllerName(controllerClass));
            controller.setName(name);
        }
    }

    private String getControllerName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        name = name.replace("Controller", "");
        name = name.replace("Resource", "");
        name = name + "Client";
        return name;
    }

    /**
     * Load the user specified controller classes mapped by the class name
     */
    private Map<String, Class<?>> loadControllerClasses(ClassLoader classLoader) throws MojoFailureException {
        Map<String, Class<?>> result = new HashMap<>();
        for (Controller controllerConfiguration : controllers) {
            try {
                Class<?> clazz = classLoader.loadClass(controllerConfiguration.getImplementation());
                result.put(controllerConfiguration.getImplementation(), clazz);
            } catch (ClassNotFoundException e) {
                throw new MojoFailureException("Could not scan class", e);
            }
        }
        return result;
    }

    private Map<String, ClientGeneratorFactory.ClientGenerator> loadGenerators() {
        Map<String, ClientGeneratorFactory.ClientGenerator> result = new HashMap<>();
        for (Controller config : controllers) {
            String generatorName = StringUtils.defaultIfEmpty(config.getGenerator(), generator);
            ClientGeneratorFactory.ClientGenerator generator = generatorFactory.getClientGenerator(generatorName);
            result.put(config.getName(), generator);
        }
        return result;
    }

    private List<ClientGeneratorConfiguration> loadConfigurations(Map<String, Class<?>> controllersClasses) {
        List<ClientGeneratorConfiguration> configs = new ArrayList<>();
        for (Controller controllerConfiguration : controllers) {
            if (!controllersClasses.containsKey(controllerConfiguration.getImplementation())) {
                continue;
            }

            Class<?> clazz = controllersClasses.get(controllerConfiguration.getImplementation());

            // continue if class isn't an actual controller
            if (!(clazz.isAnnotationPresent(org.springframework.stereotype.Controller.class)
                    || clazz.isAnnotationPresent(RestController.class))) {
                getLog().warn("Class " + clazz.getName() + " is not an actual controller! " +
                        "Class must be annotated with " + org.springframework.stereotype.Controller.class.getName() +
                        " or " + RestController.class.getName());
                continue;
            }

            ClientGeneratorConfiguration config = new ClientGeneratorConfiguration();
            config.setControllerClass(clazz);
            config.setName(controllerConfiguration.getName());
            config.setBaseURL(ObjectUtils.defaultIfNull(controllerConfiguration.getBaseUrl(), baseUrl));
            configs.add(config);
        }
        return configs;
    }

    private void loadDeclarations(ClientGeneratorConfiguration configuration) throws MojoFailureException {
        List<ClientGeneratorControllerDeclaration> declarations = new ArrayList<>();
        for (Method method : configuration.getControllerClass().getMethods()) {
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
            decleration.setMethods(requestMapping.method());
            decleration.setHeaders(requestMapping.headers());
            decleration.setParams(requestMapping.params());
            decleration.setConsumes(
                    getConsumes(requestMapping));
            decleration.setProduces(
                    getProduces(requestMapping));
            declarations.add(decleration);
        }
        configuration.setControllerDeclarations(declarations);
    }

    private String getPath(RequestMapping mapping) throws MojoFailureException {
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

    private void renderClients(List<ClientGeneratorConfiguration> configurations,
                               Map<String, ClientGeneratorFactory.ClientGenerator> generators)
            throws MojoFailureException {
        boolean isFile = target.isFile() || target.getName().contains(".");
        boolean isDirectory = !isFile;

        if (configurations.size() == 1 && isFile) {
            ClientGeneratorConfiguration config = configurations.get(0);
            ClientGenerator generator = generators.get(config.getName());
            writeClient(target, callClientGenerator(generator, config));
        } else {
            if (isFile) {
                getLog().warn("A specific file is specified to write clients to but multiple " +
                        "controllers are found. " +
                        "All generated clients will be overwritten by the next one!");
            }

            for (ClientGeneratorConfiguration config : configurations) {
                ClientGeneratorFactory.ClientGenerator generator =
                        generators.get(config.getName());

                String source = callClientGenerator(generator, config);

                File file;
                if (isDirectory) {
                    file = new File(target.getAbsolutePath() +
                            File.separator +
                            config.getName() +
                            "." +
                            generator.getFileEnding());
                } else {
                    file = target;
                }

                writeClient(file, source);
            }
        }
    }

    private String callClientGenerator(ClientGenerator clientGenerator, ClientGeneratorConfiguration config) throws MojoFailureException {
        try {
            return clientGenerator.render(config);
        } catch (Exception e) {
            throw new MojoFailureException("Failed to render clients: " + e.getMessage(), e);
        }
    }

    private void writeClient(File file, String source) throws MojoFailureException {
        try {
            FileUtils.forceMkdirParent(file);
        } catch (IOException e) {
            throw new MojoFailureException("Couldn't create parent directories for file " + file.getAbsolutePath(), e);
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new MojoFailureException("Couldn't write client file to " + file.getAbsolutePath(), e);
        }

        PrintWriter printer;
        try {
            printer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            throw new MojoFailureException("File not found " + file.getAbsolutePath(), e);
        }

        printer.print(source);
        printer.flush();
        printer.close();
    }
}
