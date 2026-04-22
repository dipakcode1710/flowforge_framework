package flowforge.cli;

import java.io.File;
import java.io.FileWriter;

public class ProjectGenerator {

    public static void generate(String name) {

        try {
            // =========================
            // Root folder
            // =========================
            File root = new File(name);
            root.mkdirs();

            // =========================
            // Proper Maven structure
            // =========================
            File javaDir = new File(root, "src/main/java/example");
            javaDir.mkdirs();

            // =========================
            // AppMain.java
            // =========================
            write(new File(javaDir, "AppMain.java"), """
            package example;

            import flowforge.Flow;

            public class AppMain {

                public static void main(String[] args) {
                    Flow.run(AppMain.class);
                }
            }
            """);

            // =========================
            // UserController.java
            // =========================
            write(new File(javaDir, "UserController.java"), """
            package example;

            import flowforge.core.annotations.*;

            @Controller
            public class UserController {

                @Get("/hello")
                public String hello() {
                    return "Hello FlowForge 🚀";
                }

                @Post("/create")
                public String create(@RequestBody String body) {
                    return "Created: " + body;
                }
            }
            """);

            // =========================
            // app.properties
            // =========================
            write(new File(root, "app.properties"), """
            server.port=9091
            app.name=MyFlowForgeApp
            """);

            // =========================
            // pom.xml (🔥 KEY PART)
            // =========================
            write(new File(root, "pom.xml"), """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">

                <modelVersion>4.0.0</modelVersion>

                <groupId>example</groupId>
                <artifactId>%s</artifactId>
                <version>1.0-SNAPSHOT</version>

                <properties>
                    <maven.compiler.source>17</maven.compiler.source>
                    <maven.compiler.target>17</maven.compiler.target>
                </properties>

                <dependencies>
                    <!-- 🔥 Your framework dependency -->
                    <dependency>
                        <groupId>flowforge</groupId>
                        <artifactId>flowforge-core</artifactId>
                        <version>1.0</version>
                    </dependency>
                </dependencies>

                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.10.1</version>
                        </plugin>
                    </plugins>
                </build>

            </project>
            """.formatted(name));

            System.out.println("✅ Project created successfully: " + name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write(File file, String content) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}