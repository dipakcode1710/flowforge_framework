package flowforge.core.middleware;

import java.util.List;

public class MiddlewareChain {

    private final List<Middleware> middlewares;
    private int index = 0;
    private final ControllerInvoker invoker;

    public MiddlewareChain(List<Middleware> middlewares, ControllerInvoker invoker) {
        this.middlewares = middlewares;
        this.invoker = invoker;
    }

    public Object proceed(RequestContext ctx) throws Exception {

        if (index < middlewares.size()) {
            Middleware current = middlewares.get(index++);

            // BEFORE
            current.before(ctx);

            Object result = current.around(ctx, this);

            // AFTER
            current.after(ctx, result);

            return result;
        }

        return invoker.invoke(ctx);
    }
}