package springmvcclientgen.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @RequestMapping(path = "/exampleBla", params = "myParam=bla")
    public String someResourceBla() {
        return "";
    }

    @RequestMapping(path = "/example/{test1}/test2/{test2}")
    public String someResourceWithParams(
            @RequestParam("test1") String test1,
            @RequestParam("test2") String test2) {
        return "";
    }
}
