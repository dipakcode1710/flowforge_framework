package example;

import flowforge.core.middleware.*;

public class LogMiddleware implements Middleware {

    @Override
    public void before(RequestContext ctx) {
        System.out.println("👉 BEFORE: " + ctx.exchange.getRequestURI());
    }

    @Override
    public void after(RequestContext ctx, Object result) {
        System.out.println("👉 AFTER: " + result);
    }
}