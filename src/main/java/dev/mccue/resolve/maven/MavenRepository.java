package dev.mccue.resolve.maven;

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
import dev.mccue.resolve.core.compatibility.SaxParsingException;
import dev.mccue.resolve.core.compatibility.Utilities;

public final class MavenRepository {
    private final String root;
    private final Optional<Authentication> authentication;
    private boolean sbtAttrStub;

    private static final Pattern SNAPSHOT_TIMESTAMP =
            Pattern.compile("(.*-)?[0-9]{8}\\.[0-9]{6}-[0-9]+");

    public static boolean isSnapshot(String version) {
        return version.endsWith("SNAPSHOT")
                || SNAPSHOT_TIMESTAMP.matcher(version).matches();
    }

    public static String toBaseVersion(String version) {
        switch (version) {
            //TODO

        }
        return version;
    }

    public static Optional<String> mavenVersioning (
        SnapshotVersioning snapshotVersioning,
        Classifier classifier,
        Extension extension
    ) {
        return Optional.empty();
    }

    private static String dirModuleName(Module module, Boolean sbtAttrStub) {
        if (sbtAttrStub) {
            var name = module.name().value();
            return name;
        } else {
            return module.name().value();
        }
    }

    private static Project parseRawPomSax(String str) throws SaxParsingException {
        try {
            return Utilities.xmlParseSax(str, new PomParser()).project();
        } catch (SaxParsingException e) { throw e;
        }
    }

    private static String actualRoot(String root) {
        if (root.endsWith("/")) 
            return root.substring(0, root.length() - 1);
        else 
            return root;
    }    

    public static MavenRepository apply(String root) {
        return new MavenRepository(actualRoot(root));
    }
    public static MavenRepository apply(String root, Optional<Authentication> authentication) {
        return new MavenRepository(root, authentication);
    }
    
    public MavenRepository(
        String root
    ) {
        this.root = root;
        this.authentication = Optional.empty();
    }

    public MavenRepository(
        String root,
        Optional<Authentication> authentication
    ) {
        this.root = root;
        this.authentication = authentication;
        this.sbtAttrStub = true;
    }

    public MavenRepository(
        String root,
        Optional<Authentication> authentication,
        boolean sbtAttrStub
    ) {
        this.root = root;
        this.authentication = authentication;
        this.sbtAttrStub = sbtAttrStub;
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

    public MavenRepository withChanging(boolean changing) {
        return this;
    }

    private List<String> modulePath(Module module)  {
        var list = Arrays.asList(module.organization().value().split("."));
        list.add(dirModuleName(module, sbtAttrStub));
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

    public Artifact projectArtifact(
        Module module,
        String version,
        Optional<String> versioningValue
    ) {
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
