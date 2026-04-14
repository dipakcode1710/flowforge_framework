package example;

import flowforge.Flow;
import flowforge.core.security.JwtUtil;

public class AppMain {

    public static void main(String[] args) {

        // 🔥 Generate token (TEMPORARY for testing)
        System.out.println(JwtUtil.generateToken("dipak", "ADMIN"));

        // Start your framework
        Flow.run(AppMain.class);
    }
}