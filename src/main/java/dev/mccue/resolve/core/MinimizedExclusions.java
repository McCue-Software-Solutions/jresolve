package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.Tuple4;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/MinimizedExclusions.scala")
public final class MinimizedExclusions {
    static final MinimizedExclusions NONE = new MinimizedExclusions(ExcludeNone.INSTANCE);
    static final MinimizedExclusions ALL = new MinimizedExclusions(ExcludeAll.INSTANCE);

    private int hash;

    private final ExclusionData exclusionData;

    private MinimizedExclusions(ExclusionData exclusionData) {
        this.hash = 0;
        this.exclusionData = Objects.requireNonNull(
                exclusionData,
                "exclusionData must not be null"
        );
    }

    public static MinimizedExclusions of(
            Set<Exclusion> exclusions
    ) {
        return of(List.copyOf(exclusions));
    }

    public static MinimizedExclusions of(
            List<Exclusion> exclusions
    ) {
        if (exclusions.isEmpty()) {
            return NONE;
        }

        var excludeByGroup0 = new HashSet<GroupId>();
        var excludeByArtifact0 = new HashSet<ArtifactId>();
        var remaining0 = new HashSet<Exclusion>();

        for (var exclusion : exclusions) {
            if (GroupId.ALL.equals(exclusion.groupId())) {
                if (ArtifactId.ALL.equals(exclusion.artifactId())) {
                    return ALL;
                } else {
                    excludeByArtifact0.add(exclusion.artifactId());
                }
            } else if (ArtifactId.ALL.equals(exclusion.artifactId())) {
                excludeByGroup0.add(exclusion.groupId());
            } else {
                remaining0.add(exclusion);
            }
        }

        return new MinimizedExclusions(new ExcludeSpecific(
                Set.copyOf(excludeByGroup0),
                Set.copyOf(excludeByArtifact0),
                remaining0.stream()
                        .filter(exclusion ->
                                !excludeByGroup0.contains(exclusion.groupId())
                                    && !excludeByArtifact0.contains(exclusion.artifactId()))
                        .collect(Collectors.toUnmodifiableSet()))
        );
    }

    public boolean shouldInclude(GroupId groupId, ArtifactId artifactId) {
        return this.exclusionData.shouldInclude(groupId, artifactId);
    }

    public MinimizedExclusions join(MinimizedExclusions other) {
        var newData = this.exclusionData.join(other.exclusionData);
        if (newData == this.exclusionData) {
            return this;
        }
        else if (newData == other.exclusionData) {
            return other;
        }
        else {
            return new MinimizedExclusions(newData);
        }
    }

    public MinimizedExclusions meet(MinimizedExclusions other) {
        var newData = this.exclusionData.meet(other.exclusionData);
        if (newData == this.exclusionData) {
            return this;
        }
        else if (newData == other.exclusionData) {
            return other;
        }
        else {
            return new MinimizedExclusions(newData);
        }
    }

    public MinimizedExclusions map(Function<String, String> f) {
        var newData = this.exclusionData.map(f);
        if (newData == this.exclusionData) {
            return this;
        }
        else {
            return new MinimizedExclusions(newData);
        }
    }

    public Tuple4<Boolean,
            Set<GroupId>,
            Set<ArtifactId>,
            Set<Exclusion>
            > partitioned() {
        return this.exclusionData.partitioned();
    }


    public int size() {
        return this.exclusionData.size();
    }

    public boolean subsetOf(MinimizedExclusions other) {
        return this.exclusionData.subsetOf(other.exclusionData);
    }

    public Set<Exclusion> toSet() {
        return this.exclusionData.toSet();
    }

    public sealed interface ExclusionData {
        boolean shouldInclude(
                GroupId groupId,
                ArtifactId artifactId
        );

        ExclusionData join(ExclusionData other);
        ExclusionData meet(ExclusionData other);

        Tuple4<
                Boolean,
                Set<GroupId>,
                Set<ArtifactId>,
                Set<Exclusion>
                > partitioned();

        ExclusionData map(Function<String, String> f);

        int size();

        boolean subsetOf(ExclusionData other);

        Set<Exclusion> toSet();
    }

    public enum ExcludeNone implements ExclusionData {
        INSTANCE;

        @Override
        public boolean shouldInclude(GroupId groupId, ArtifactId artifactId) {
            return true;
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return other;
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return ExcludeNone.INSTANCE;
        }

        @Override
        public Tuple4<Boolean, Set<GroupId>, Set<ArtifactId>, Set<Exclusion>> partitioned() {
            return new Tuple4<>(
                    false,
                    Set.of(),
                    Set.of(),
                    Set.of()
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return this;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return true;
        }

        @Override
        public Set<Exclusion> toSet() {
            return Set.of();
        }


        @Override
        public String toString() {
            return "ExcludeNone";
        }
    }

    public enum ExcludeAll implements ExclusionData {
        INSTANCE;

        @Override
        public boolean shouldInclude(GroupId groupId, ArtifactId artifactId) {
            return false;
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return this;
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return other;
        }

        @Override
        public Tuple4<Boolean, Set<GroupId>, Set<ArtifactId>, Set<Exclusion>> partitioned() {
            return new Tuple4<>(
                    true,
                    Set.of(),
                    Set.of(),
                    Set.of()
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return this;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return this.equals(other);
        }

        @Override
        public Set<Exclusion> toSet() {
            return Set.of(new Exclusion(GroupId.ALL, ArtifactId.ALL));
        }

        @Override
        public String toString() {
            return "ExcludeAll";
        }
    }

    public record ExcludeSpecific(
            Set<GroupId> byGroupId,
            Set<ArtifactId> byArtifactId,
            Set<Exclusion> specific
    ) implements ExclusionData {
        public ExcludeSpecific(
                Set<GroupId> byGroupId,
                Set<ArtifactId> byArtifactId,
                Set<Exclusion> specific
        ) {
            this.byGroupId = Set.copyOf(byGroupId);
            this.byArtifactId = Set.copyOf(byArtifactId);
            this.specific = Set.copyOf(specific);
        }

        @Override
        public boolean shouldInclude(GroupId groupId, ArtifactId artifactId) {
            return !this.byGroupId.contains(groupId)
                    && !this.byArtifactId.contains(artifactId)
                    && !this.specific.contains(new Exclusion(groupId, artifactId));
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> this;
                case ExcludeAll all -> all;
                case ExcludeSpecific(
                        Set<GroupId> otherByOrg,
                        Set<ArtifactId> otherByArtifactId,
                        Set<Exclusion> otherSpecific
                ) -> {

                    var joinedByOrg = new HashSet<GroupId>();
                    joinedByOrg.addAll(this.byGroupId);
                    joinedByOrg.addAll(otherByOrg);

                    var joinedByModule = new HashSet<ArtifactId>();
                    joinedByModule.addAll(this.byArtifactId);
                    joinedByModule.addAll(otherByArtifactId);

                    var joinedSpecific = new HashSet<Exclusion>();
                    this.specific
                            .stream()
                            .filter(exclusion ->
                                    !otherByOrg.contains(exclusion.groupId()) &&
                                        !otherByArtifactId.contains(exclusion.artifactId()))
                            .forEach(joinedSpecific::add);

                    otherSpecific
                            .stream()
                            .filter(exclusion ->
                                    !byGroupId.contains(exclusion.groupId()) &&
                                            !byArtifactId.contains(exclusion.artifactId()))
                            .forEach(joinedSpecific::add);

                    yield new ExcludeSpecific(
                            Set.copyOf(joinedByOrg),
                            Set.copyOf(joinedByModule),
                            Set.copyOf(joinedSpecific)
                    );
                }
            };
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return switch (other) {
                case ExcludeNone none -> none;
                case ExcludeAll __ -> this;
                case ExcludeSpecific(
                        Set<GroupId> otherByGroupId,
                        Set<ArtifactId> otherByArtifactId,
                        Set<Exclusion> otherSpecific
                )  -> {
                    var metByGroup = byGroupId.stream()
                            .filter(otherByGroupId::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metByArtifact = byArtifactId.stream()
                            .filter(otherByArtifactId::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metSpecific = new HashSet<Exclusion>();
                    specific.stream()
                            .filter(exclusion -> {
                                var groupId = exclusion.groupId();
                                var artifactId = exclusion.artifactId();
                                return otherByGroupId.contains(groupId) ||
                                        otherByArtifactId.contains(artifactId) ||
                                        otherSpecific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    otherSpecific.stream()
                            .filter(exclusion -> {
                                var groupId = exclusion.groupId();
                                var artifactId = exclusion.artifactId();
                                return byGroupId.contains(groupId) ||
                                        byArtifactId.contains(artifactId) ||
                                        specific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    if (metByGroup.isEmpty() && metByArtifact.isEmpty() && metSpecific.isEmpty()) {
                        yield ExcludeNone.INSTANCE;
                    }
                    else {
                        yield new ExcludeSpecific(
                                metByGroup,
                                metByArtifact,
                                Set.copyOf(metSpecific)
                        );
                    }


                }
            };
        }

        @Override
        public Tuple4<Boolean, Set<GroupId>, Set<ArtifactId>, Set<Exclusion>> partitioned() {
            return new Tuple4<>(
                    false,
                    byGroupId,
                    byArtifactId,
                    specific
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return new ExcludeSpecific(
                    byGroupId.stream()
                            .map(org -> org.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    byArtifactId.stream()
                            .map(moduleName -> moduleName.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    specific.stream()
                            .map(exclusion -> new Exclusion(
                                    exclusion.groupId().map(f),
                                    exclusion.artifactId().map(f)
                            ))
                            .collect(Collectors.toUnmodifiableSet())
            );
        }

        @Override
        public int size() {
            return byGroupId.size() + byArtifactId().size() + specific.size();
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> false;
                case ExcludeAll __ -> false; // This seems wrong
                case ExcludeSpecific excludeSpecific ->
                        excludeSpecific.byGroupId.containsAll(byGroupId)
                        && excludeSpecific.byArtifactId.containsAll(byArtifactId)
                        && excludeSpecific.specific.containsAll(specific);
            };
        }

        @Override
        public Set<Exclusion> toSet() {
            var set = new HashSet<Exclusion>();
            byGroupId.stream()
                    .map(org -> new Exclusion(org, ArtifactId.ALL))
                    .forEach(set::add);
            byArtifactId.stream()
                    .map(moduleName -> new Exclusion(GroupId.ALL, moduleName))
                    .forEach(set::add);
            set.addAll(specific);

            return Set.copyOf(set);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (
                obj instanceof MinimizedExclusions minimizedExclusions &&
                this.exclusionData.equals(minimizedExclusions.exclusionData)
        );
    }

    @Override
    public int hashCode() {
        int cached = this.hash;
        if (cached == 0) {
            cached = this.exclusionData.hashCode();
            this.hash = cached;
            return cached;
        }
        else {
            return this.hash;
        }
    }

    @Override
    public String toString() {
        return "MinimizedExclusions[" +
                "exclusionData=" + exclusionData +
                ']';
    }
}
