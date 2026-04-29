package flowforge.cli;

import java.io.File;
import java.io.FileWriter;

/**
 * The ProjectGenerator is responsible for scaffolding a new FlowForge project on disk.
 *
 * Generates a standard Maven project structure including source files, resources,
 * and a pre-configured pom.xml ready to run with the FlowForge framework.
 *
 * @author Dipak Suryawanshi
 * @since 1.0
 */
public class ProjectGenerator {

    /**
     * Generates a new FlowForge project in a directory named after the given project name.
     *
     * The generated project includes:
     *   - A Maven source layout under src/main/java/example/
     *   - AppMain.java         : the application entry point
     *   - UserController.java  : a sample controller with GET and POST routes
     *   - app.properties       : default server configuration
     *   - pom.xml              : Maven build file with FlowForge, exec, and shade plugins
     *
     * @param name the name of the project and the root directory to create
     */
    public static void generate(String name) {

        try {
            // Root folder
            File root = new File(name);
            root.mkdirs();

            // Proper Maven structure
            File javaDir = new File(root, "src/main/java/example");
            javaDir.mkdirs();

            // AppMain.java
            write(new File(javaDir, "AppMain.java"), """
            package example;

            import flowforge.Flow;

            public class AppMain {

                public static void main(String[] args) {
                    Flow.run(AppMain.class);
                }
            }
            """);

            // UserController.java
            write(new File(javaDir, "UserController.java"), """
            package example;

            import flowforge.core.annotations.*;

            @Controller
            public class UserController {

                @Get("/hello")
                public String hello() {
                    return "Hello FlowForge";
                }

                @Post("/create")
                public String create(@RequestBody String body) {
                    return "Created: " + body;
                }
            }
            """);

            // app.properties
            File resDir = new File(root, "src/main/resources");
            resDir.mkdirs();

            write(new File(resDir, "app.properties"), """
            server.port=9091
            app.name=MyFlowForgeApp
            """);

            // pom.xml
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
            		        <!-- FlowForge Framework -->
            		        <dependency>
            		            <groupId>flowforge</groupId>
            		            <artifactId>flowforge-core</artifactId>
            		            <version>1.0</version>
            		        </dependency>
            		    </dependencies>

            		    <build>
            		        <plugins>

            		            <!-- Compiler -->
            		            <plugin>
            		                <groupId>org.apache.maven.plugins</groupId>
            		                <artifactId>maven-compiler-plugin</artifactId>
            		                <version>3.11.0</version>
            		            </plugin>

            		            <!-- Run app -->
            		            <plugin>
            		                <groupId>org.codehaus.mojo</groupId>
            		                <artifactId>exec-maven-plugin</artifactId>
            		                <version>3.1.0</version>
            		                <configuration>
            		                    <mainClass>example.AppMain</mainClass>
            		                </configuration>
            		            </plugin>

            		            <!-- Fat jar -->
            		            <plugin>
            		                <groupId>org.apache.maven.plugins</groupId>
            		                <artifactId>maven-shade-plugin</artifactId>
            		                <version>3.5.0</version>
            		                <executions>
            		                    <execution>
            		                        <phase>package</phase>
            		                        <goals>
            		                            <goal>shade</goal>
            		                        </goals>
            		                    </execution>
            		                </executions>
            		            </plugin>

            		        </plugins>
            		    </build>

            		</project>
            		""".formatted(name));

            System.out.println("Project created successfully: " + name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the given content to the specified file.
     *
     * @param file    the file to write to
     * @param content the text content to write
     */
    private static void write(File file, String content) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}