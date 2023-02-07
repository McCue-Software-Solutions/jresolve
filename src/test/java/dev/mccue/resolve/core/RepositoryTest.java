package dev.mccue.resolve.core;

import dev.mccue.resolve.maven.MavenRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {
    @Test
    public void testDefaults() {
        assertInstanceOf(MavenRepository.class, Repository.defaults().get(0));
    }

    @Test
    public void testCentral() {
        assertInstanceOf(MavenRepository.class, Repository.central());
        assertEquals("repo1.maven.org/maven2", Repository.central().root());
    }
}
