package flowforge.cli;

public class FlowForgeCLI {

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("❌ Missing arguments");
            System.out.println("Usage: flowforge create app <name>");
            return;
        }

        if (args[0].equalsIgnoreCase("create")
                && args[1].equalsIgnoreCase("app")) {

            String appName = args[2];

            ProjectGenerator.generate(appName);
        } else {
            System.out.println("❌ Invalid command");
            System.out.println("Usage: flowforge create app <name>");
        }
    }
}