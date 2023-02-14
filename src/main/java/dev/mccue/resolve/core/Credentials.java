package dev.mccue.resolve.core;

import java.io.Serializable;

public record Credentials(String user, String password) implements Serializable {
    @Override
    public int hashCode() {
        return 37 * (17 + user.hashCode()) + password.hashCode();
    }

    @Override
    public String toString() {
        return "Credentials(" + user + ", ****)";
    }
}
