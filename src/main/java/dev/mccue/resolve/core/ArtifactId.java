package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L18-L26")
public record ArtifactId(String value) implements Comparable<ArtifactId> {
    public static ArtifactId ALL = new ArtifactId("*");
    public ArtifactId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public ArtifactId map(Function<String, String> f) {
        return new ArtifactId(f.apply(this.value));
    }

    @Override
    public int compareTo(ArtifactId o) {
        return this.value.compareTo(o.value);
    }
}
