package flowforge.core.middleware;

public interface ControllerInvoker {
    Object invoke(RequestContext ctx) throws Exception;
}