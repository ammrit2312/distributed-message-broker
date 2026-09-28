package com.broker.server;

public class BrokerConfig {

    private final int brokerId;
    private final int port;
    private final boolean isLeader;
    private final String postgresUrl;
    private final String postgresUser;
    private final String postgresPassword;
    private final String followerHosts;
    private final String leaderHosts;

    public BrokerConfig() {
        this.brokerId = Integer.parseInt(System.getenv().getOrDefault("BROKER_ID", "1"));
        this.port = Integer.parseInt(System.getenv().getOrDefault("PORT", "9001"));
        this.isLeader = Boolean.parseBoolean(System.getenv().getOrDefault("IS_LEADER", "true"));
        this.postgresUrl = System.getenv().getOrDefault("POSTGRES_URL", "jdbc:postgresql://localhost:5432/broker_db");
        this.postgresUser = System.getenv().getOrDefault("POSTGRES_USER", "broker");
        this.postgresPassword = System.getenv().getOrDefault("POSTGRES_PASSWORD", "broker123");
        this.followerHosts = System.getenv().getOrDefault("FOLLOWER_HOSTS", "");
        this.leaderHosts = System.getenv().getOrDefault("LEADER_HOSTS", "");
    }

    public int getBrokerId() {
        return brokerId;
    }

    public int getPort() {
        return port;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public String getPostgresUrl() {
        return postgresUrl;
    }

    public String getPostgresUser() {
        return postgresUser;
    }

    public String getPostgresPassword() {
        return postgresPassword;
    }

    public String getFollowerHosts() {
        return followerHosts;
    }

    public String getLeaderHosts() {
        return leaderHosts;
    }

    @Override
    public String toString() {
        return "BrokerConfig{ brokerId = "+brokerId+", port = "+port+", isLeader = "+isLeader+", postgresUrl = '"+postgresUrl+"', postgresUser = '"+postgresUser+"', followerHosts = '"+followerHosts+"', leaderHosts = '"+leaderHosts+"' }";
    }
}