package example;

import flowforge.core.annotations.Service;

@Service
public class UserService {

    public String getMessage() {
        return "Hello from Service 🔥";
    }
}