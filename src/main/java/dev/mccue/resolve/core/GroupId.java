package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L8-L9")
public record GroupId(String value) implements Comparable<GroupId> {
    public static GroupId ALL = new GroupId("*");
    public GroupId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public GroupId map(Function<String, String> f) {
        return new GroupId(f.apply(this.value));
    }

    @Override
    public int compareTo(GroupId o) {
        return this.value.compareTo(o.value);
    }
}
