package dev.mccue.resolve.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Objects;

import dev.mccue.resolve.util.Authentication;
import dev.mccue.resolve.util.Artifact;
import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Extension;
import dev.mccue.resolve.core.Module;
import dev.mccue.resolve.core.SnapshotVersioning;
import dev.mccue.resolve.core.Project;
import dev.mccue.resolve.core.SnapshotVersion;
import dev.mccue.resolve.core.compatibility.Utilities;

public final class MavenRepository {
    private final String root;
    private final Optional<Authentication> authentication;

    private static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile("(.*-)?[0-9]{8}\\.[0-9]{6}-[0-9]+");

    public static boolean isSnapshot(String version) {
        return version.endsWith("SNAPSHOT")
                || SNAPSHOT_TIMESTAMP.matcher(version).matches();
    }

    public static String toBaseVersion(String version) {
        switch (version) {
            // TODO

        }
        return version;
    }

    public static Optional<String> mavenVersioning(
            SnapshotVersioning snapshotVersioning,
            Classifier classifier,
            Extension extension) {
        return snapshotVersioning
                .snapshotVersions()
                .stream()
                .filter((SnapshotVersion v) ->
                        (v.classifier().equals(classifier) || v.classifier().equals(new Classifier("*"))) &&
                        (v.extension().equals(extension) || v.extension().equals(new Extension("*"))))
                .findFirst()
                .map(SnapshotVersion::value)
                .filter(s -> !s.isEmpty());// I think this is right, coursier uses an extra .filter?
    }

    private static Project parseRawPomSax(String str) {
        return Utilities.xmlParseSax(str, new PomParser()).project();
    }

    private static String actualRoot(String root) {
        if (root.endsWith("/")) {
            return root.substring(0, root.length() - 1);
        } else {
            return root;
        }
    }

    public MavenRepository(
            String root) {
        this.root = actualRoot(root);
        this.authentication = Optional.empty();
    }

    public MavenRepository(
            String root,
            Optional<Authentication> authentication) {
        this.root = actualRoot(root);
        this.authentication = authentication;
    }

    public String root() {
        return this.root;
    }

    public Optional<Authentication> authentication() {
        return this.authentication;
    }

    @Override
    public String toString() {
        return "MavenRepository[name=" + this.root + ", authentication=" + this.authentication + "]";
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof MavenRepository other &&
                Objects.equals(this.root, other.root) &&
                Objects.equals(this.authentication, other.authentication));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.root) * Objects.hash(this.authentication);
    }

    private List<String> modulePath(Module module) {
        var list = new ArrayList<>(Arrays.asList(module.organization().value().split("\\.")));
        list.add(module.name().value());
        return list;
    }

    private List<String> moduleVersionPath(Module module, String version) {
        var list = modulePath(module);
        list.add(toBaseVersion(version));
        return list;
    }

    public String urlFor(List<String> path) {
        var b = new StringBuilder(root);
        b.append('/');

        final var it = path.iterator();
        var isFirst = true;
        while (it.hasNext()) {
            if (isFirst) {
                isFirst = false;
            } else {
                b.append('/');
            }
            b.append(it.next());
        }

        return b.toString();
    }

    public String urlFor(List<String> path, Boolean isDir) {
        var b = new StringBuilder(root);
        b.append('/');

        final var it = path.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (isDir) {
                b.append('/');
            }
        }

        return b.toString();
    }

    public Artifact projectArtifact(
            Module module,
            String version,
            Optional<String> versioningValue) {
        var path = moduleVersionPath(module, version);
        path.add(String.format("%s-%s.pom", module.name().value(), versioningValue.orElse(version)));

        return new Artifact(
                urlFor(path),
                Map.of(),
                Map.of(),
                false,
                false,
                authentication);
    }

}
