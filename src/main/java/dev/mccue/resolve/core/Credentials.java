package dev.mccue.resolve.core;

public record Credentials(String user, String password) {
    @Override
    public String toString() {
        return "Credentials[user=" + user + ", password=***]";
    }
}
