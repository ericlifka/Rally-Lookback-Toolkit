package com.rallydev.lookback;

public class LookbackApi {

    String server = "https://rally1.rallydev.com";
    String versionMajor = "2";
    String versionMinor = "0";

    String workspace;
    String username;
    String password;

    public LookbackApi setCredentials(String username, String password) {
        this.username = username;
        this.password = password;

        return this;
    }

    public LookbackApi setServer(String server) {
        this.server = server;

        return this;
    }

    public LookbackApi setWorkspace(String workspace) {
        this.workspace = workspace;

        return this;
    }

    public LookbackApi setVersion(String major, String minor) {
        this.versionMajor = major;
        this.versionMinor = minor;

        return this;
    }

    public LookbackQuery newSnapshotQuery() {
        return new LookbackQuery(this);
    }

    void executeQuery(String requestJson) {

    }
}
