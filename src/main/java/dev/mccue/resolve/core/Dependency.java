package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;

import java.util.Objects;
import java.util.Set;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Dependency.scala")
public record Dependency(
        Library library,
        String version,
        Configuration configuration,
        MinimizedExclusions minimizedExclusions,
        Publication publication,
        boolean optional,
        boolean transitive
) {
    public Dependency(
            Library library,
            String version,
            Configuration configuration,
            MinimizedExclusions minimizedExclusions,
            Publication publication,
            boolean optional,
            boolean transitive
    ) {
        this.library = Objects.requireNonNull(
                library,
                "library must not be null");
        this.version = Objects.requireNonNull(
                version,
                "version must not be null"
        );
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null"
        );
        this.minimizedExclusions = Objects.requireNonNull(
                minimizedExclusions,
                "minimizedExclusions must not be null"
        );
        this.publication = Objects.requireNonNull(
                publication,
                "publication must not be null"
        );
        this.optional = optional;
        this.transitive = transitive;
    }

    public Dependency(
            Library library,
            String version
    ) {
        this(
                library,
                version,
                Configuration.EMPTY,
                MinimizedExclusions.NONE,
                Publication.EMPTY,
                false,
                true
        );
    }

    public Dependency(
            Library library,
            String version,
            Configuration configuration,
            Set<Exclusion> exclusions,
            Attributes attributes,
            boolean optional,
            boolean transitive
    ) {
        this(
                library,
                version,
                configuration,
                MinimizedExclusions.of(exclusions),
                attributes.publication("", Extension.EMPTY),
                optional,
                transitive
        );
    }

    public Dependency(String group, String artifact, String version) {
        this(new Library(new GroupId(group), new ArtifactId(artifact)), version);
    }

    public Dependency withVersion(String version) {
        return new Dependency(
                this.library,
                version,
                this.configuration,
                this.minimizedExclusions,
                this.publication,
                this.optional,
                this.transitive);
    }

    public String getVersion() {
        return this.version;
    }

    public Library getLibrary() {
        return this.library;
    }

    public int compareVersion(Dependency other) {
        return this.version.compareTo(other.version);
    }
}
