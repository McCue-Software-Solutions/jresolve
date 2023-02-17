package dev.mccue.resolve.maven;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MavenRepositoryTest {
    @Test
    public void testIsSnapshot() {
        String s1 = "Hello";
        String s2 = "HelloSNAPSHOT";

        assertFalse(MavenRepository.isSnapshot(s1));
        assertTrue(MavenRepository.isSnapshot(s2));
    }

    @Test
    public void testMavenRepositoryBasics() {
        MavenRepository repo = new MavenRepository("test");
        assertEquals("test", repo.root());
        assertEquals("MavenRepository[name=test, authentication=Optional.empty]", repo.toString());

        MavenRepository repo2 = new MavenRepository("test/");
        assertEquals("test", repo.root());

        MavenRepository repo3 = new MavenRepository("test/");
        assertTrue(repo2.equals(repo3));
    }

    @Test
    public void testUrlFor() {
        MavenRepository repo = new MavenRepository("repo1.maven.org/maven2/");
        List<String> path = new ArrayList();
        path.add("args4j");
        path.add("args4j");
        path.add("2.33");
        assertEquals("repo1.maven.org/maven2/args4j/args4j/2.33", repo.urlFor(path));
    }
}
