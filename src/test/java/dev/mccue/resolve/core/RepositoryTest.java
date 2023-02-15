package dev.mccue.resolve.core;

import dev.mccue.resolve.maven.MavenRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {
    @Test
    public void testDefaults() {
        assertTrue(Repository.defaults().contains(Repository.central()));
    }

    @Test
    public void testCentral() {
        assertEquals("repo1.maven.org/maven2", Repository.central().root());
    }
}
