package springmvcclientgen.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Robin Engel
 */
@RestController
public class ExampleController {

    @RequestMapping(value = "/example", params = "!myParam")
    public String someResource() {
        return "";
    }

    @RequestMapping(value = "/example", params = "myParam=bla")
    public String someResourceBla() {
        return "";
    }
}
