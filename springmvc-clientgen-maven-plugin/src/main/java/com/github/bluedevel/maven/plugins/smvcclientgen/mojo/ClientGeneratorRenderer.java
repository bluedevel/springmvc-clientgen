package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Robin Engel on 23.03.17.
 */
public class ClientGeneratorRenderer {

    private File target;
    private Log log;

    public ClientGeneratorRenderer(File target, Log log) {
        this.target = target;
        this.log = log;
    }

    // TODO paralleling render process
    /**
     * Renders and writes a {@link EnhancedClientGenConfig} to the file or directory
     * configured by the plugin settings.
     */
    public void render(EnhancedClientGenConfig config) {
        boolean isFile = target.isFile() || target.getName().contains(".");

        ClientGeneratorFactory.ClientGenerator generator = config.getGenerator();
        String source = callClientGenerator(generator, config);

        File file;
        if (isFile) {
            file = target;
            log.warn("A single file is specified to write clients to. " +
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
            StreamExceptions.throwSilent(new MojoFailureException(
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
            StreamExceptions.throwSilent(new MojoFailureException(
                    "Couldn't create parent directories for file " + file.getAbsolutePath(), e));
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            StreamExceptions.throwSilent(new MojoFailureException(
                    "Couldn't write client file to " + file.getAbsolutePath(), e));
        }

        PrintWriter printer;
        try {
            printer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            StreamExceptions.throwSilent(new MojoFailureException(
                    "File not found " + file.getAbsolutePath(), e));
            // never reached, but a hack for exception handling with the stream api
            return;
        }

        printer.print(source);
        printer.flush();
        printer.close();
    }

}
