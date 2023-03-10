package dev.mccue.resolve.api;

import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.maven.MockRepository;
import dev.mccue.resolve.maven.ModelParseException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveTest {
    @Test
    public void testResolve() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("org.clojure", "clojure", "1.11.0"));
        r.run();
        assertEquals(Set.of(
                new Dependency("org.clojure", "clojure", "1.11.0"),
                new Dependency("org.clojure", "core.specs.alpha", "0.2.62"),
                new Dependency("org.clojure", "spec.alpha", "0.3.218")
            ), mock.downloaded);
    }
}
