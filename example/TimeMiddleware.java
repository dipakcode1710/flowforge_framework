package example;

import flowforge.core.middleware.*;

public class TimeMiddleware implements Middleware {

    @Override
    public Object around(RequestContext ctx, MiddlewareChain next) throws Exception {

        long start = System.currentTimeMillis();

        Object result = next.proceed(ctx);

        long end = System.currentTimeMillis();

        System.out.println("⏱ Execution time: " + (end - start) + " ms");

        return result;
    }
}