package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.core.JavaScriptClientGenerator;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Engel
 */
public class ClientGeneratorFactory {

    private Map<String, ClientGeneratorWrapper> generators;
    private ClassLoader classLoader;

    public ClientGeneratorFactory() {
        reset();
        classLoader = System.class.getClassLoader();
    }

    public ClientGeneratorWrapper getClientGenerator(String generatorName) {
        ClientGeneratorWrapper generator = generators.get(generatorName);

        if (generator == null) {
            throw new IllegalArgumentException("Unknown generator " + generatorName);
        }

        return generator;
    }

    public void registerGenerators(Generator... generators) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (generators == null) {
            return;
        }

        for (Generator generator : generators) {
            throw new NotImplementedException("Registering custom generators is not yet implemented");
        }
    }

    public void registerDefaultGenerators() {
        generators.put("javascript", new ClientGeneratorWrapper(new JavaScriptClientGenerator(), "js"));
    }

    public void reset() {
        generators = new HashMap<String, ClientGeneratorWrapper>();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static class ClientGeneratorWrapper {

        private ClientGenerator clientGenerator;
        private String fileEnding;

        public ClientGeneratorWrapper() {
        }

        public ClientGeneratorWrapper(ClientGenerator clientGenerator, String fileEnding) {
            this.clientGenerator = clientGenerator;
            this.fileEnding = fileEnding;
        }

        public ClientGenerator getClientGenerator() {
            return clientGenerator;
        }

        public void setClientGenerator(ClientGenerator clientGenerator) {
            this.clientGenerator = clientGenerator;
        }

        public String getFileEnding() {
            return fileEnding;
        }

        public void setFileEnding(String fileEnding) {
            this.fileEnding = fileEnding;
        }
    }
}
