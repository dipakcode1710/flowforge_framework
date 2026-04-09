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
    public User save(User user) {
        return user;
    }

    @Get("/user")
    public User getUser() {
        return new User("Dipak", 25);
    }

    @Get("/test")
    public String test() {
        return service.getMessage();
    }
}
