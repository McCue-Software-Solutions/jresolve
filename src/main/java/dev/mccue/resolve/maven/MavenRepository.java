package dev.mccue.resolve.maven;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Objects;

import dev.mccue.resolve.util.Authentication;

public final class MavenRepository {
    private final String root;
    private final Optional<Authentication> authentication;

    private static final Pattern SNAPSHOT_TIMESTAMP =
            Pattern.compile("(.*-)?[0-9]{8}\\.[0-9]{6}-[0-9]+");

    public static boolean isSnapshot(String version) {
        return version.endsWith("SNAPSHOT")
                || SNAPSHOT_TIMESTAMP.matcher(version).matches();
    }

    public static String toBaseVersion(String version) {
        switch (version) {

        }
        return version;
    }

    public MavenRepository(
        String root,
        Optional<Authentication> authentication
    ) {
        this.root = root;
        this.authentication = authentication;
    }

    public String root() { return this.root; }
    public Optional<Authentication> authentication() { return this.authentication; }

    @Override
    public String toString() {
        return "MavenRepository[name=" + this.root + ", authentication=" + this.authentication + "]";
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (
            o instanceof MavenRepository other &&
            Objects.equals(this.root, other.root) &&
            Objects.equals(this.authentication, other.authentication)
        );
    }

    @Override
    public int hashCode() {
        return this.root.hashCode() * this.authentication.hashCode();
    }

    public String urlFor(List<String> path) {
        var b = new StringBuilder(root);
        b.append('/');

        final var it = path.iterator();
        var isFirst = true;
        while (it.hasNext()) {
            if (isFirst) 
                isFirst = false;
            else
                b.append('/');
            it.next();
        }

        return b.toString();
    }

    public String urlFor(List<String> path, Boolean isDir) {
        var b = new StringBuilder(root);
        b.append('/');

        final var it = path.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (isDir)
                b.append('/');
        }

        return b.toString();
    }


}