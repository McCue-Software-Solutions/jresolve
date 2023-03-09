package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockRepositoryTest {
    MockRepository mock = new MockRepository("graph1");
    Dependency dependency = new Dependency("org.clojure", "clojure", "1.11.0");

    @Test
    public void testGetPom() {
        var pom = mock.getPom(dependency);
        assertDoesNotThrow(() -> PomParser.parsePom(pom));
    }

    @Test
    public void testDownload() {
        mock.download(dependency, Extension.POM, Classifier.EMPTY);
        assertEquals(Set.of(dependency), mock.downloaded);
    }
}
