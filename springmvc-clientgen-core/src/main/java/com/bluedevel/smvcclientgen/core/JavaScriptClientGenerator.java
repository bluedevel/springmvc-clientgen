package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;

/**
 * @author Robin Engel
 */
public class JavaScriptClientGenerator implements ClientGenerator {

    public String render(ClientGeneratorConfiguration config) {
        return config.getControllerClass().getSimpleName() + " " + config.getControllerMethod().getName();
    }

}
