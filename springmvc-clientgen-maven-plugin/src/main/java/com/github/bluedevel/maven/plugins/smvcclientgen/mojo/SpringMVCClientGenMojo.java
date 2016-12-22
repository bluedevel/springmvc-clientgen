package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Robin Engel
 */
@Mojo(name = "generate-clients")
public class SpringMVCClientGenMojo extends AbstractMojo {

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        URL outputURL;
        try {
            outputURL = outputDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Failed to find output folder", e);
        }

        ClassLoader parentClassLoader = this.getClass().getClassLoader();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{outputURL}, parentClassLoader);


    }

}
