package flowforge.cli;

public class FlowForgeCLI {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: flowforge create app <name>");
            return;
        }

        String command = args[0];

        switch (command) {

            case "create":
                if (args.length >= 3 && args[1].equals("app")) {
                    ProjectGenerator.generate(args[2]);
                } else {
                    System.out.println("Usage: flowforge create app <name>");
                }
                break;

            case "run":
                runApp();
                break;

            case "build":
                buildApp();
                break;

            default:
                System.out.println("Unknown command: " + command);
        }
    }

    private static void runApp() {
        try {
            System.out.println("🚀 Running app...");
            new ProcessBuilder("mvn", "exec:java")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildApp() {
        try {
            System.out.println("📦 Building app...");
            new ProcessBuilder("mvn", "clean", "package")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}