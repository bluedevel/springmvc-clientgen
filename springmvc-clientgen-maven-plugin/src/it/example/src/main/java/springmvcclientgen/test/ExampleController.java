package springmvcclientgen.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Robin Engel
 */
@RestController
public class ExampleController {

    @RequestMapping(path = "/example")
    public String someResource() {
        return "";
    }

}
