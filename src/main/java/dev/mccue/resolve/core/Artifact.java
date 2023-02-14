package dev.mccue.resolve.core;

import java.util.Objects;

public record Artifact(String url, boolean changing, boolean optional, Credentials credentials) {
    @Override
    public int hashCode() {
        return 37 * (37 * (37 * (17 + url.hashCode()) + Boolean.hashCode(changing)) + Boolean.hashCode(optional)) + Objects.hashCode(credentials);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Artifact(");
        b.append(url);
        b.append(", optional = ");
        b.append(optional);
        b.append(", changing = ");
        b.append(changing);
        if (credentials != null) {
            b.append(", ");
            b.append(credentials);
        }
        b.append(")");
        return b.toString();
    }
}
