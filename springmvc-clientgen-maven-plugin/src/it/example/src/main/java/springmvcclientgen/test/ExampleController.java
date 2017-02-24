package springmvcclientgen.test;

import org.springframework.web.bind.annotation.PathVariable;
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
            @PathVariable("test1") String test1,
            @PathVariable("test2") String test2,
            @RequestParam("query1") String query1,
            @RequestParam("query2") String query2) {
        return "";
    }
}
