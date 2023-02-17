package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Coursier(
        value = "https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L28-L79",
        details = "Did not translate the lazy hash code or instance caching."
)
public record GroupAndArtifact(
        Organization organization,
        ModuleName name
) {
    public GroupAndArtifact(
            Organization organization,
            ModuleName name
    ) {
        this.organization = Objects.requireNonNull(
                organization,
                "organization must not be null"
        );
        this.name = Objects.requireNonNull(
                name,
                "name must not be null"
        );
    }

    @Override
    public String toString() {
        return organization.value() + ":" + name.value();
    }
}
