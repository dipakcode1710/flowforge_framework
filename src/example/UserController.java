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
        return "Middleware Working 🔥";
    }
    @Get("/user")
    public String getUser(@QueryParam(value="id", required=false) Integer id) {
        return "User ID: " + id;
    } 

    */
    @Auth
    @Get("/secure")
    public String secure() {
        return "Protected Data 🔒";
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
        return "Admin API 🔒";
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
}