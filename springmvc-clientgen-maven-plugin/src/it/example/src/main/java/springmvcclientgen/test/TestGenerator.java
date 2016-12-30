package springmvcclientgen.test;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;

/**
 * @author Robin Engel
 */
public class TestGenerator implements ClientGenerator {

    public String render(ClientGeneratorConfiguration config) {
        return "test " + config.getControllerClass().getSimpleName() + " " + config.getControllerDeclarations().get(0).getName();
    }
}
