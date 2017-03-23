package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;

/**
 * Enhances {@link ClientGeneratorConfiguration} with data needed inside the controller processing stream.
 *
 * @author Robin Engel on 23.03.17.
 */
public class EnhancedClientGenConfig extends ClientGeneratorConfiguration {

    private Controller controller;
    private ClientGeneratorFactory.ClientGenerator generator;

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public ClientGeneratorFactory.ClientGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(ClientGeneratorFactory.ClientGenerator generator) {
        this.generator = generator;
    }
}
