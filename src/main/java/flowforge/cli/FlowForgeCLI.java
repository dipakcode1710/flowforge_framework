package flowforge.cli;

/**
 * The FlowForgeCLI is the command-line interface entry point for the FlowForge framework.
 *
 * Supported commands:
 *   - create app <name> : Generates a new FlowForge project
 *   - run               : Runs the application using Maven
 *   - build             : Builds the application using Maven
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
public class FlowForgeCLI {

    /**
     * The main entry point for the FlowForge CLI.
     *
     * Parses the provided command-line arguments and delegates to the appropriate handler.
     *
     * @param args the command-line arguments
     */
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

    /**
     * Runs the application by invoking mvn exec:java as a subprocess.
     *
     * The subprocess inherits the standard input, output, and error streams of the current
     * process and blocks until it completes.
     */
    private static void runApp() {
        try {
            System.out.println("Running app...");
            new ProcessBuilder("mvn", "exec:java")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the application by invoking mvn clean package as a subprocess.
     *
     * The subprocess inherits the standard input, output, and error streams of the current
     * process and blocks until it completes.
     */
    private static void buildApp() {
        try {
            System.out.println("Building app...");
            new ProcessBuilder("mvn", "clean", "package")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}