package example;

import flowforge.core.annotations.*;
import flowforge.core.server.ErrorResponse;

@Controller
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ErrorResponse handleRuntime(RuntimeException e) {
        return new ErrorResponse("RUNTIME_ERROR", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleBadRequest(Exception e) {
        return new ErrorResponse("BAD_REQUEST", e.getMessage());
    }
}