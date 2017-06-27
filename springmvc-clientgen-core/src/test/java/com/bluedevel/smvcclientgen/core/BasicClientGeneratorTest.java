package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.Parameter;
import com.bluedevel.smvcclientgen.ResourceHandler;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.bluedevel.smvcclientgen.Parameter.Type.PATH;
import static com.bluedevel.smvcclientgen.Parameter.Type.QUERY;
import static org.junit.Assert.assertEquals;

/**
 * @author Robin Engel
 */
public class BasicClientGeneratorTest {

    @Test
    public void testRegression() throws Exception {
        ClientGenerator clientGenerator = new BasicClientGenerator("test");
        ClientGeneratorConfiguration config = new ClientGeneratorConfiguration();

        config.setBaseURL(new URL("http://test.bla"));
        config.setName("testName");
        List<ResourceHandler> resourceHandlers = new ArrayList<>();
        config.setResourceHandlers(resourceHandlers);

        ResourceHandler handler1 = new ResourceHandler();
        resourceHandlers.add(handler1);
        handler1.setName("handler1");
        handler1.setPath("/path/1");
        handler1.setConsumes("consumes1");
        handler1.setMethods(new String[]{"GET", "POST"});

        List<Parameter> parameters = new ArrayList<>();
        handler1.setParameters(parameters);

        parameters.add(new Parameter("p1", PATH));
        parameters.add(new Parameter("p2", QUERY));

        String result = clientGenerator.render(config);

        String expected = "testName,http://test.bla\n" +
                "    name:getHandler1;path:/path/1;method:GET;consumes:consumes1\n" +
                "        param:p1;type:PATH\n" +
                "        param:p2;type:QUERY\n" +
                "        name:postHandler1;path:/path/1;method:POST;consumes:consumes1\n" +
                "        param:p1;type:PATH\n" +
                "        param:p2;type:QUERY";

        assertEquals(expected.trim(), result.trim());
    }

}