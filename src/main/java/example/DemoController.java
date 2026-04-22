package example;

import flowforge.core.annotations.*;

@Auth
@Tag("Demo APIs")
@Controller
public class DemoController {
	
    @Get("/user")
    public String user() {
        return "User API";
    }

    @Tag("Admin APIs")
    @Get("/admin")
    public String admin() {
        return "Admin API";
    }
}