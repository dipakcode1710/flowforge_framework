package example;

import flowforge.core.annotations.Value;

public class UserService {

    @Value(value = "app.name", defaultValue = "DefaultApp")
    private String appName;

    public String getAppName() {
        return appName;
    }
}