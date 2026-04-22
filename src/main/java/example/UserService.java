package example;

import flowforge.core.annotations.Service;
import flowforge.core.annotations.Value;

@Service
public class UserService {

    @Value(value = "app.name", defaultValue = "DefaultApp")
    private String appName;

    public String getAppName() {
        return appName;
    }
}