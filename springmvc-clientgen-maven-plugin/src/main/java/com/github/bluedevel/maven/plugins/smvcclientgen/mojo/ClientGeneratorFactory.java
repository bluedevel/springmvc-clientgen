package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.core.JavaScriptClientGenerator;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Engel
 */
public class ClientGeneratorFactory {

    private Map<String, ClientGenerator> generators;

    public ClientGeneratorFactory() {
        reset();
    }

    public ClientGenerator getClientGenerator(String generatorName) {
        ClientGenerator generator = generators.get(generatorName);

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
        generators.put("javascript",
                new ClientGenerator(new JavaScriptClientGenerator("javascript"), "js"));
        generators.put("jquery",
                new ClientGenerator(new JavaScriptClientGenerator("jquery"), "js"));
    }

    public void reset() {
        generators = new HashMap<String, ClientGenerator>();
    }

    public static class ClientGenerator implements com.bluedevel.smvcclientgen.ClientGenerator {

        private com.bluedevel.smvcclientgen.ClientGenerator clientGenerator;
        private String fileEnding;

        private ClientGenerator(com.bluedevel.smvcclientgen.ClientGenerator clientGenerator, String fileEnding) {
            this.clientGenerator = clientGenerator;
            this.fileEnding = fileEnding;
        }

        public String getFileEnding() {
            return fileEnding;
        }

        @Override
        public String render(ClientGeneratorConfiguration config) throws Exception {
            return clientGenerator.render(config);
        }
    }
}
