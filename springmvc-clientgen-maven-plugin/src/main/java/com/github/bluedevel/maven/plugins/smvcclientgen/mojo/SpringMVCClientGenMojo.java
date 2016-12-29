package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.stereotype.Controller;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Robin Engel
 */
@Mojo(name = "generate-clients")
public class SpringMVCClientGenMojo extends AbstractMojo {

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    @Parameter
    private String[] classesToScan;

    @Parameter
    private String generator;

    @Parameter
    private File target;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (classesToScan == null || classesToScan.length == 0) {
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

        Set<Class<?>> classes = loadClassesToScan(classLoader);
        List<ClientGeneratorConfiguration> configurations = getClientGeneratorConfigurations(classes);
        renderClients(configurations);
    }

    private Set<Class<?>> loadClassesToScan(URLClassLoader classLoader) throws MojoFailureException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (String className : classesToScan) {
            Class<?> clazz;
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new MojoFailureException("Could not scan class", e);
            }

            if (clazz.isAnnotationPresent(Controller.class)
                    || clazz.isAnnotationPresent(RestController.class)) {
                classes.add(clazz);
            }
        }
        return classes;
    }

    private List<ClientGeneratorConfiguration> getClientGeneratorConfigurations(Set<Class<?>> classes) {
        List<ClientGeneratorConfiguration> configurations = new ArrayList<ClientGeneratorConfiguration>();
        for (Class clazz : classes) {
            for (Method method : clazz.getMethods()) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

                if (requestMapping == null) {
                    continue;
                }

                ClientGeneratorConfiguration config = new ClientGeneratorConfiguration();
                config.setControllerClass(clazz);
                config.setControllerMethod(method);
                config.setName(requestMapping.name());
                config.setPath(requestMapping.path());
                config.setMethod(requestMapping.method());
                config.setHeaders(requestMapping.headers());
                config.setParams(requestMapping.params());
                config.setConsumes(requestMapping.consumes());
                config.setProduces(requestMapping.produces());
                configurations.add(config);
            }
        }
        return configurations;
    }

    private void renderClients(List<ClientGeneratorConfiguration> configurations) throws MojoFailureException {
        if (configurations.size() == 0) {
            return;
        }

        ClientGenerator clientGenerator = ClientGeneratorFactory.getClientGenerator(generator);

        boolean isFile = target.isFile() || target.getName().contains(".");
        boolean isDirectory = !isFile;

        if (configurations.size() == 1 && isFile) {
            writeClient(target, clientGenerator.render(configurations.get(0)));
        } else {
            if (isFile) {
                getLog().warn("A specific file is specified to write clients to but multiple " +
                        "controllers are found. All generated clients will be overwritten by the next one!");
            }

            for (ClientGeneratorConfiguration configuration : configurations) {
                String source = clientGenerator.render(configuration);
                File file;
                if (isDirectory) {
                    String name = configuration.getControllerClass().getSimpleName();
                    name = name.replace("Controller", "");
                    name = name.replace("Resource", "");
                    name = name + "Client";

                    file = new File(target.getAbsolutePath() + File.separator + name + ".js");
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
}
