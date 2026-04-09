package example;

import flowforge.core.annotations.*;

@Controller
public class UserController {

    @Inject
    UserService service;
    
    @Get("/hello")
    public String hello() {
        return "Hello Dipak!";
    }

    @Post("/save")
    public String save(String body) {
        return "Received: " + body;
    }

    // ✅ ADD THIS HERE
    @Get("/user")
    public User getUser() {
        return new User("Dipak", 25);
    }
    
    @Get("/test")
    public String test() {
        return service.getMessage();
    }    
}