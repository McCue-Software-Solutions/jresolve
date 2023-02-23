package dev.mccue.resolve.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class MinimizedExclusionsTest {
    @Test
    public void emptyExclusionsToSet() {
        assertEquals(MinimizedExclusions.NONE.toSet(), Set.of());
    }

    @Test
    public void allExclusionsToSet() {
        assertEquals(MinimizedExclusions.ALL.toSet(), Set.of(
                new Exclusion(GroupId.ALL, ArtifactId.ALL)
        ));
    }

    @Test
    public void regularExclusionsToSet() {
        var exclusions = MinimizedExclusions.of(Set.of(
                new Exclusion(new GroupId("apple"), ArtifactId.ALL),
                new Exclusion(GroupId.ALL, new ArtifactId("metaverse")),
                new Exclusion(new GroupId("com.google"), new ArtifactId("abc"))
        ));

        assertEquals(exclusions.toSet(), Set.of(
                new Exclusion(new GroupId("apple"), ArtifactId.ALL),
                new Exclusion(GroupId.ALL, new ArtifactId("metaverse")),
                new Exclusion(new GroupId("com.google"), new ArtifactId("abc"))
        ));
    }

    @Test
    public void allModuleExclusionsReplaceMoreSpecificExclusions() {
        assertEquals(
                MinimizedExclusions.of(Set.of(
                        new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                        new Exclusion(new GroupId("facebook"), ArtifactId.ALL)
                )).toSet(),
                Set.of(new Exclusion(new GroupId("facebook"), ArtifactId.ALL))
        );
    }

    @Test
    public void allOrganizationExclusionsReplaceMoreSpecificExclusions() {
        assertEquals(
                MinimizedExclusions.of(Set.of(
                        new Exclusion(GroupId.ALL, new ArtifactId("metaverse")),
                        new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                        new Exclusion(new GroupId("facebook"), new ArtifactId("whatever"))
                )).toSet(),
                Set.of(
                        new Exclusion(GroupId.ALL, new ArtifactId("metaverse")),
                        new Exclusion(new GroupId("facebook"), new ArtifactId("whatever"))
                )
        );
    }

    @Test
    public void joiningWithAllExclusionsGetsJustAllExclusions() {
        assertEquals(
                MinimizedExclusions.of(Set.of(Exclusion.ALL)),
                MinimizedExclusions.of(Set.of(Exclusion.ALL)).join(
                    MinimizedExclusions.of(
                            Set.of(
                                    new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                                    new Exclusion(new GroupId("facebook"), new ArtifactId("whatever"))
                            )
                    )
                )
        );

        assertEquals(
                MinimizedExclusions.of(Set.of(Exclusion.ALL)),
                MinimizedExclusions.of(
                        Set.of(
                                new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                                new Exclusion(new GroupId("facebook"), new ArtifactId("whatever"))
                        )
                ).join(
                        MinimizedExclusions.of(Set.of(
                                new Exclusion(GroupId.ALL, ArtifactId.ALL)
                        ))
                )
        );
    }

    @Test
    public void excludeAllExcludesAll() {
        assertFalse(MinimizedExclusions.ALL.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertFalse(MinimizedExclusions.ALL.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
    }

    @Test
    public void excludeNoneExcludesNone() {
        assertTrue(MinimizedExclusions.NONE.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertTrue(MinimizedExclusions.NONE.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
    }

    @Test
    public void excludeSpecificDep() {
        var excl = MinimizedExclusions.of(List.of(
                        new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse"))
        ));
        assertFalse(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
        assertTrue(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("def")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("metaverse")));
    }

    @Test
    public void excludeAllWithArtifact() {
        var excl = MinimizedExclusions.of(List.of(
                new Exclusion(new GroupId("*"), new ArtifactId("metaverse"))
        ));
        assertFalse(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertFalse(excl.shouldInclude(new GroupId("google"), new ArtifactId("metaverse")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
        assertTrue(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("def")));
    }

    @Test
    public void excludeAllInGroup() {
        var excl = MinimizedExclusions.of(List.of(
                new Exclusion(new GroupId("google"), new ArtifactId("*"))
        ));
        assertFalse(excl.shouldInclude(new GroupId("google"), new ArtifactId("guice")));
        assertFalse(excl.shouldInclude(new GroupId("google"), new ArtifactId("guava")));
        assertTrue(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("guice")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("metaverse")));
    }

    @Test
    public void meetWithNone() {
        assertEquals(
                MinimizedExclusions.NONE,
                MinimizedExclusions.of(List.of(
                        new Exclusion(new GroupId("abc"), new ArtifactId("def"))
                )).meet(MinimizedExclusions.NONE)
        );

        assertEquals(
                MinimizedExclusions.NONE,
                MinimizedExclusions.ALL.meet(MinimizedExclusions.NONE)
        );

        assertEquals(
                MinimizedExclusions.NONE,
                MinimizedExclusions.NONE.meet(MinimizedExclusions.NONE)
        );
    }
}
