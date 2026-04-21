package example;

import flowforge.core.annotations.*;

@Auth
@Controller
public class UserController {
	
    @Inject
    private UserService service;
    @Inject
    private ServerConfig config;   // FIXED
    @Inject
    private AppHelper helper;    

    /*@Get("/user/{id}")
    public String getUser(@PathVariable("id") int id) {
        return "User ID: " + id;
    }

    @Get("/user/{id}/order/{orderId}")
    public String getOrder(
            @PathVariable("id") int id,
            @PathVariable("orderId") int orderId) {

        return "User: " + id + ", Order: " + orderId;
    }*/
	
    //@Before(LogMiddleware.class)
    //@Around(TimeMiddleware.class)
    //@After(LogMiddleware.class)
	/*
    @Get("/test")
    public String test() {
        return "Middleware Working ";
    }
    @Get("/user")
    public String getUser(@QueryParam(value="id", required=false) Integer id) {
        return "User ID: " + id;
    } 

    */
    @Auth
    @Get("/secure")
    public String secure() {
        return "Protected Data ";
    }
    
    @Get("/secure1")
    public String a() {
        return "A";
    }

    @Get("/secure2")
    public String b() {
        return "B";
    } 
    
    @Auth
    @Get("/user")
    public String user() {
        return "User API";
    }
    
    @Auth(role = "ADMIN")
    @Get("/admin")
    public String admin() {
        return "Admin API ";
    }
    
    
    @Get("/test")
    public String test(@QueryParam("id") @NotNull @Min(5) Integer id) {
        return "ID: " + id;
    }
    

    @Get("/app")
    public String app() {
        return "App: " + service.getAppName();
    } 
    
    @Get("/server")
    public String server() {
        return "Port: " + config.port + ", Name: " + config.name;
    } 
    
    @Get("/comp")
    public String test() {
        return helper.getMessage();
    }
    
    @Get("/search")
    public String search(
        @QueryParam("name") String name,
        @QueryParam("age") Integer age,
        @QueryParam("city") String city
    ) {
        return "Name=" + name + ", Age=" + age + ", City=" + city;
    } 
    
    @Get("/filter")
    public String filter(
        @QueryParam("category") String category,
        @QueryParam(value="page", required=false) Integer page,
        @QueryParam(value="size", required=false) Integer size
    ) {
        return "Category=" + category + ", Page=" + page + ", Size=" + size;
    } 
    
    @Get("/user/{id}/orders")
    public String orders(
        @PathVariable("id") int id,
        @QueryParam("status") String status,
        @QueryParam("limit") int limit
    ) {
        return "User=" + id + ", Status=" + status + ", Limit=" + limit;
    } 
    
    @Get("/validate")
    public String validate(
        @QueryParam("id") @Min(5) int id,
        @QueryParam("name") @NotNull String name
    ) {
        return "Valid ID=" + id + ", Name=" + name;
    } 
    
    @Get("/edge")
    public String edge(
        @QueryParam("a") String a,
        @QueryParam("b") String b
    ) {
        return "A=" + a + ", B=" + b;
    } 
    
    @Get("/combo")
    public String combo(
        @PathVariable("id") int id,
        @QueryParam("q") String q,
        @QueryParam("page") int page
    ) {
        return "ID=" + id + ", Q=" + q + ", Page=" + page;
    }  
    
    @Post("/create-user")
    public String createUser(@RequestBody UserRequest req) {
        return "Created: " + req.name + " (" + req.age + ")";
    }    
}