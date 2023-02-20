package dev.mccue.resolve.core;

import java.util.Objects;

public record Exclusion(
        GroupId groupId,
        ArtifactId artifactId
) {
    public static final Exclusion ALL = new Exclusion(GroupId.ALL, ArtifactId.ALL);

    public Exclusion {
        Objects.requireNonNull(groupId, "organization must not be null");
        Objects.requireNonNull(artifactId, "moduleName must not be null");
    }
}
