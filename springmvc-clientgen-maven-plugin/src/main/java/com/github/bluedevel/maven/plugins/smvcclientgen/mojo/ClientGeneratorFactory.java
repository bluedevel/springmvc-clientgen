package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.core.JavaScriptClientGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Engel
 */
public class ClientGeneratorFactory {

    private static Map<String, ClientGenerator> generators;

    static {
        generators = new HashMap<String, ClientGenerator>();
        generators.put("javascript", new JavaScriptClientGenerator());
    }

    public static ClientGenerator getClientGenerator(String generatorName) {
        ClientGenerator generator = generators.get(generatorName);

        if (generator == null) {
            throw new IllegalArgumentException("Unknown generator " + generatorName);
        }

        return generator;
    }

}
