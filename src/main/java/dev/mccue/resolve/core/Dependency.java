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
        Exclusions exclusions,
        Publication publication,
        boolean optional,
        boolean transitive
) {
    public Dependency(
            Library library,
            String version,
            Configuration configuration,
            Exclusions exclusions,
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
        this.exclusions = Objects.requireNonNull(
                exclusions,
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
                Exclusions.NONE,
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
                Exclusions.of(exclusions),
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
                this.exclusions,
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
        // vnum stores each numeric part of version
        int vnum1 = 0, vnum2 = 0;

        // loop until both String are processed
        for (int i = 0, j = 0; (i < this.version().length()
                || j < other.version().length()); ) {
            // Storing numeric part of
            // version 1 in vnum1
            while (i < this.version().length()
                    && this.version().charAt(i) != '.') {
                vnum1 = vnum1 * 10
                        + (this.version().charAt(i) - '0');
                i++;
            }

            // storing numeric part
            // of version 2 in vnum2
            while (j < other.version().length()
                    && other.version().charAt(j) != '.') {
                vnum2 = vnum2 * 10
                        + (other.version().charAt(j) - '0');
                j++;
            }

            if (vnum1 > vnum2)
                return 1;
            if (vnum2 > vnum1)
                return -1;

            // if equal, reset variables and
            // go for next numeric part
            vnum1 = vnum2 = 0;
            i++;
            j++;
        }
        return 0;
    }
}
