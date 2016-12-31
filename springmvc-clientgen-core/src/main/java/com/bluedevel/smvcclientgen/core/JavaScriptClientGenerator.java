package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.ClientGenerator;
import com.bluedevel.smvcclientgen.ClientGeneratorConfiguration;
import com.bluedevel.smvcclientgen.ClientGeneratorControllerDecleration;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * @author Robin Engel
 */
public class JavaScriptClientGenerator implements ClientGenerator {

    public String render(ClientGeneratorConfiguration config) throws Exception {
        if (config.getControllerDeclarations().size() == 0) {
            return "";
        }

        ClientGeneratorControllerDecleration decleration = config.getControllerDeclarations().get(0);

        StringWriter writer = new StringWriter();
        InputStream inputStream = this.getClass().getResourceAsStream("templates/javascript.js.template");
        InputStreamReader templateReader = new InputStreamReader(inputStream);

        IOUtils.copy(templateReader, writer);

        StringTemplate template = new StringTemplate();
        template.setTemplate(writer.toString());

        template.setAttribute("path", decleration.getPath()[0]);
        template.setAttribute("method",
                decleration.getMethod().length > 0 ? decleration.getMethod()[0] : RequestMethod.GET);

        return template.toString();
    }

}
