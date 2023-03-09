package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


public class MavenRepositoryTest {
    MavenRepository maven = new MavenRepository();
    Dependency dependency = new Dependency("org.clojure", "clojure", "1.11.0");

    @Test
    public void testUriOf() {
        var actual = maven.uriOf(dependency, Extension.POM, Classifier.EMPTY);
        var expected = URI.create("https://repo.maven.apache.org/maven2/org/clojure/clojure/1.11.0/clojure-1.11.0.pom");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPom() {
        var pom = maven.getPom(dependency);
        assertDoesNotThrow(() -> PomParser.parsePom(pom));
    }
}
