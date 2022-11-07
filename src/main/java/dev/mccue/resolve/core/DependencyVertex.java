package dev.mccue.resolve.core;

import java.util.Objects;

public record DependencyVertex (String artifactID, String groupID, String version){
    public DependencyVertex(String artifactID, String groupID, String version) {
        this.artifactID = Objects.requireNonNull(artifactID, "artifactID must not be null");
        this.groupID = Objects.requireNonNull(groupID, "groupID must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
    }
}
