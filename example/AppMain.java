package example;

import flowforge.Flow;
import flowforge.core.annotations.App;

@App
public class AppMain {
    public static void main(String[] args) {
        Flow.run(AppMain.class);
    }
}