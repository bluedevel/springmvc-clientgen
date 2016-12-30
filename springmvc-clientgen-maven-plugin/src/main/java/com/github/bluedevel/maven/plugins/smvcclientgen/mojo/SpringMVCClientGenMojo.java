package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDecleration;
import org.apache.commons.io.FileUtils;
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
import java.util.List;

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

    private ClientGeneratorFactory generatorFactory;

    public SpringMVCClientGenMojo() {
        generatorFactory = new ClientGeneratorFactory();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (controllers == null || controllers.length == 0) {
            return;
        }

        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        URL outputURL;
        try {
            outputURL = outputDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Failed to find output folder", e);
        }

        ClassLoader parentClassLoader = this.getClass().getClassLoader();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{outputURL}, parentClassLoader);

        configureGeneratorFactory(classLoader);

        List<Configuration> configs = loadClassesToScan(classLoader);
        loadClientGeneratorConfigurations(configs);
        renderClients(configs);
    }

    private void configureGeneratorFactory(ClassLoader classLoader) throws MojoFailureException {
        generatorFactory.reset();
        generatorFactory.registerDefaultGenerators();

        generatorFactory.setClassLoader(classLoader);
        try {
            generatorFactory.registerGenerators(generators);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new MojoFailureException("Couldn't register generators", e);
        }
    }

    private List<Configuration> loadClassesToScan(URLClassLoader classLoader) throws MojoFailureException {
        List<Configuration> configs = new ArrayList<>();
        for (Controller controller : controllers) {
            Class<?> clazz;
            try {
                clazz = classLoader.loadClass(controller.getImplementation());
            } catch (ClassNotFoundException e) {
                throw new MojoFailureException("Could not scan class", e);
            }

            if (clazz.isAnnotationPresent(org.springframework.stereotype.Controller.class)
                    || clazz.isAnnotationPresent(RestController.class)) {
                Configuration config = new Configuration();
                ClientGeneratorConfiguration clientGeneratorConfiguration = new ClientGeneratorConfiguration();
                clientGeneratorConfiguration.setControllerClass(clazz);
                config.setControllerConfig(controller);
                config.setClientGeneratorConfiguration(clientGeneratorConfiguration);
                configs.add(config);
            }
        }
        return configs;
    }

    private void loadClientGeneratorConfigurations(List<Configuration> configs) {
        for (Configuration config : configs) {
            List<ClientGeneratorControllerDecleration> declarations = new ArrayList<>();
            for (Method method : config.getClientGeneratorConfiguration().getControllerClass().getMethods()) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

                if (requestMapping == null) {
                    continue;
                }

                ClientGeneratorControllerDecleration decleration = new ClientGeneratorControllerDecleration();
                decleration.setControllerMethod(method);
                decleration.setName(requestMapping.name());
                decleration.setPath(requestMapping.path());
                decleration.setMethod(requestMapping.method());
                decleration.setHeaders(requestMapping.headers());
                decleration.setParams(requestMapping.params());
                decleration.setConsumes(requestMapping.consumes());
                decleration.setProduces(requestMapping.produces());
                declarations.add(decleration);
            }
            config.getClientGeneratorConfiguration().setControllerDeclarations(declarations);
        }
    }

    private void renderClients(List<Configuration> configurations) throws MojoFailureException, MojoExecutionException {
        if (configurations.size() == 0) {
            return;
        }

        ClientGeneratorFactory.ClientGeneratorWrapper clientGenerator = generatorFactory.getClientGenerator(generator);

        if (clientGenerator == null) {
            throw new MojoExecutionException("No generator with name " + generator + " configured");
        }

        boolean isFile = target.isFile() || target.getName().contains(".");
        boolean isDirectory = !isFile;

        if (configurations.size() == 1 && isFile) {
            writeClient(target, clientGenerator.getClientGenerator().render(configurations.get(0).getClientGeneratorConfiguration()));
        } else {
            if (isFile) {
                getLog().warn("A specific file is specified to write clients to but multiple " +
                        "controllers are found. All generated clients will be overwritten by the next one!");
            }

            for (Configuration configuration : configurations) {
                String source = clientGenerator.getClientGenerator().render(configuration.getClientGeneratorConfiguration());
                File file;
                if (isDirectory) {
                    String name = configuration.getClientGeneratorConfiguration().getControllerClass().getSimpleName();
                    name = name.replace("Controller", "");
                    name = name.replace("Resource", "");
                    name = name + "Client";

                    file = new File(target.getAbsolutePath() + File.separator + name + "." + clientGenerator.getFileEnding());
                } else {
                    file = target;
                }

                writeClient(file, source);
            }
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

    private String capitalizeFirstLetter(String str) {
        if (str.length() == 0) {
            return "";
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static class Configuration {

        private Controller controllerConfig;
        private ClientGeneratorFactory.ClientGeneratorWrapper clientGeneratorWrapper;
        private ClientGeneratorConfiguration clientGeneratorConfiguration;

        public Controller getControllerConfig() {
            return controllerConfig;
        }

        public void setControllerConfig(Controller controllerConfig) {
            this.controllerConfig = controllerConfig;
        }

        public ClientGeneratorFactory.ClientGeneratorWrapper getClientGeneratorWrapper() {
            return clientGeneratorWrapper;
        }

        public void setClientGeneratorWrapper(ClientGeneratorFactory.ClientGeneratorWrapper clientGeneratorWrapper) {
            this.clientGeneratorWrapper = clientGeneratorWrapper;
        }

        public ClientGeneratorConfiguration getClientGeneratorConfiguration() {
            return clientGeneratorConfiguration;
        }

        public void setClientGeneratorConfiguration(ClientGeneratorConfiguration clientGeneratorConfiguration) {
            this.clientGeneratorConfiguration = clientGeneratorConfiguration;
        }
    }
}
