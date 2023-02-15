package dev.mccue.resolve.core;

import java.io.Serializable;

public record Credentials(String user, String password) implements Serializable {
    @Override
    public String toString() {
        return "Credentials[user=" + user + ", password=***]";
    }
}
