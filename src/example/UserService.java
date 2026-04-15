package example;

import flowforge.core.annotations.Value;

public class UserService {

    @Value("app.name")
    private String appName;

    public String getAppName() {
        return appName;
    }
}