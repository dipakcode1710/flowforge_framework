package flowforge.core.db;

import flowforge.core.config.Config;

public class DataSourceConfig {

    public final String url;
    public final String username;
    public final String password;
    public final String ddlAuto;

    private DataSourceConfig(String url, String username, String password, String ddlAuto) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.ddlAuto = ddlAuto;
    }

    public static DataSourceConfig load() {
        String url = Config.get("datasource.url");
        if (url == null) return null;

        String username = Config.get("datasource.username");
        String password = Config.get("datasource.password");
        if (password == null) password = "";

        String ddlAuto = Config.get("datasource.ddl-auto");
        if (ddlAuto == null) ddlAuto = "none";

        return new DataSourceConfig(url, username, password, ddlAuto);
    }
}
