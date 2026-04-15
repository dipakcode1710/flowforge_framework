package example;

import flowforge.core.annotations.ConfigurationProperties;

@ConfigurationProperties("server")
public class ServerConfig {

    public int port;
    public String name;
}