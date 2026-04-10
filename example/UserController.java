package example;

import flowforge.core.annotations.*;

@Controller
public class UserController {

    @Get("/user/{id}")
    public String getUser(@PathVariable("id") int id) {
        return "User ID: " + id;
    }

    @Get("/user/{id}/order/{orderId}")
    public String getOrder(
            @PathVariable("id") int id,
            @PathVariable("orderId") int orderId) {

        return "User: " + id + ", Order: " + orderId;
    }
}