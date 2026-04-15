package flowforge.core.middleware;

import flowforge.core.security.JwtUtil;
import io.jsonwebtoken.Claims;

import java.io.OutputStream;

public class AuthMiddleware implements Middleware {

    private String requiredRole;

    public AuthMiddleware() {}

    public AuthMiddleware(String role) {
        this.requiredRole = role;
    }

    @Override
    public Object around(RequestContext ctx, MiddlewareChain next) throws Exception {

        String authHeader = ctx.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(ctx, "Missing or invalid token");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = JwtUtil.validateToken(token);

            String userRole = claims.get("role", String.class);

            // 🔥 Role check
            if (requiredRole != null && !requiredRole.isEmpty()) {

                if (userRole == null || 
                    !userRole.equalsIgnoreCase(requiredRole)) {

                    return unauthorized(ctx, "Forbidden: Insufficient role");
                }
            }

        } catch (Exception e) {
            return unauthorized(ctx, "Invalid or expired token");
        }

        return next.proceed(ctx);
    }

    private Object unauthorized(RequestContext ctx, String message) throws Exception {

        ctx.handled = true;

        ctx.exchange.sendResponseHeaders(
                message.contains("Forbidden") ? 403 : 401,
                message.length()
        );

        OutputStream os = ctx.exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();

        return null;
    }
}