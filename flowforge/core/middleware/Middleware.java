package flowforge.core.middleware;
import flowforge.core.middleware.Middleware;

public interface Middleware {

    default void before(RequestContext ctx) {}

    default void after(RequestContext ctx, Object result) {}

    default Object around(RequestContext ctx, MiddlewareChain next) throws Exception {
        return next.proceed(ctx);
    }
}