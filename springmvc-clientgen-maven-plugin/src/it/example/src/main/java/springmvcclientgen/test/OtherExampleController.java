package springmvcclientgen.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Robin Engel
 */
@RestController
public class OtherExampleController {

    @RequestMapping(value = "/other/example", params = "!myParam")
    public String someOtherResource() {
        return "";
    }

    @RequestMapping(value = "/other/example", params = "myParam=bla")
    public String someOtherResourceBla() {
        return "";
    }
}
