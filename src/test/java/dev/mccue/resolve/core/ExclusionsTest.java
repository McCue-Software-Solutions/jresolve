package dev.mccue.resolve.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class ExclusionsTest {
    @Test
    public void emptyExclusionsToSet() {
        assertEquals(Exclusions.NONE.toSet(), Set.of());
    }

    @Test
    public void allExclusionsToSet() {
        assertEquals(Exclusions.ALL.toSet(), Set.of(
                new Exclusion(GroupId.ALL, ArtifactId.ALL)
        ));
    }

    @Test
    public void regularExclusionsToSet() {
        var exclusions = Exclusions.of(Set.of(
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
                Exclusions.of(Set.of(
                        new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                        new Exclusion(new GroupId("facebook"), ArtifactId.ALL)
                )).toSet(),
                Set.of(new Exclusion(new GroupId("facebook"), ArtifactId.ALL))
        );
    }

    @Test
    public void allOrganizationExclusionsReplaceMoreSpecificExclusions() {
        assertEquals(
                Exclusions.of(Set.of(
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
                Exclusions.of(Set.of(Exclusion.ALL)),
                Exclusions.of(Set.of(Exclusion.ALL)).join(
                        Exclusions.of(
                                Set.of(
                                        new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                                        new Exclusion(new GroupId("facebook"), new ArtifactId("whatever"))
                                )
                        )
                )
        );

        assertEquals(
                Exclusions.of(Set.of(Exclusion.ALL)),
                Exclusions.of(
                        Set.of(
                                new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse")),
                                new Exclusion(new GroupId("facebook"), new ArtifactId("whatever"))
                        )
                ).join(
                        Exclusions.of(Set.of(
                                new Exclusion(GroupId.ALL, ArtifactId.ALL)
                        ))
                )
        );
    }

    @Test
    public void excludeAllExcludesAll() {
        assertFalse(Exclusions.ALL.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertFalse(Exclusions.ALL.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
    }

    @Test
    public void excludeNoneExcludesNone() {
        assertTrue(Exclusions.NONE.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertTrue(Exclusions.NONE.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
    }

    @Test
    public void excludeSpecificDep() {
        var excl = Exclusions.of(List.of(
                new Exclusion(new GroupId("facebook"), new ArtifactId("metaverse"))
        ));
        assertFalse(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
        assertTrue(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("def")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("metaverse")));
    }

    @Test
    public void excludeAllWithArtifact() {
        var excl = Exclusions.of(List.of(
                new Exclusion(new GroupId("*"), new ArtifactId("metaverse"))
        ));
        assertFalse(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("metaverse")));
        assertFalse(excl.shouldInclude(new GroupId("google"), new ArtifactId("metaverse")));
        assertTrue(excl.shouldInclude(new GroupId("abc"), new ArtifactId("def")));
        assertTrue(excl.shouldInclude(new GroupId("facebook"), new ArtifactId("def")));
    }

    @Test
    public void excludeAllInGroup() {
        var excl = Exclusions.of(List.of(
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
                Exclusions.NONE,
                Exclusions.of(List.of(
                        new Exclusion(new GroupId("abc"), new ArtifactId("def"))
                )).meet(Exclusions.NONE)
        );

        assertEquals(
                Exclusions.NONE,
                Exclusions.ALL.meet(Exclusions.NONE)
        );

        assertEquals(
                Exclusions.NONE,
                Exclusions.NONE.meet(Exclusions.NONE)
        );
    }
}
