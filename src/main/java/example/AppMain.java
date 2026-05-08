package example;

import flowforge.Flow;
import flowforge.core.annotations.App;
import flowforge.core.security.JwtUtil;

@App
public class AppMain {

    public static void main(String[] args) {

        //  Generate token
        System.out.println(JwtUtil.generateToken("dipak", "ADMIN"));

        //  Check config
        System.out.println("PORT = " + flowforge.core.config.Config.get("server.port"));

        //  Start framework
        Flow.run(AppMain.class);
    }
}