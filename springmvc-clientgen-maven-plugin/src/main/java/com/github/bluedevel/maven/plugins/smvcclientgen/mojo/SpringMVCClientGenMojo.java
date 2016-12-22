package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
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

        Set<Class> classes = new HashSet<Class>();

        for (String className : classesToScan) {
            Class<?> clazz;
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new MojoFailureException("Could not scan class", e);
            }

            if (clazz.isAnnotationPresent(Controller.class)) {
                classes.add(clazz);
            }
        }

        getLog().info("!!!" + classes);

        List<RequestMapping> mappings = new ArrayList<RequestMapping>();
        for (Class clazz : classes) {
            for (Method method : clazz.getMethods()) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                if (requestMapping != null) {
                    mappings.add(requestMapping);
                }
            }
        }

        getLog().info("!!!" + mappings.size());
    }

}
