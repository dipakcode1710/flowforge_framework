package example;

public class User {

    public String name;
    public int age;

    // REQUIRED for Jackson
    public User() {}

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}