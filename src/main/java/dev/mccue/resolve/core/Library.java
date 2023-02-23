package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;

@Coursier(
        value = "https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L28-L79",
        details = "Did not translate the lazy hash code or instance caching."
)
public record Library(
        GroupId groupId,
        ArtifactId artifactId
) {
    public Library(
            GroupId groupId,
            ArtifactId artifactId
    ) {
        this.groupId = Objects.requireNonNull(
                groupId,
                "organization must not be null"
        );
        this.artifactId = Objects.requireNonNull(
                artifactId,
                "artifactId must not be null"
        );
    }

    @Override
    public String toString() {
        return groupId.value() + ":" + artifactId.value();
    }
}
