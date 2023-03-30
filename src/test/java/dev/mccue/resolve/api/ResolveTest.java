package dev.mccue.resolve.api;

import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.maven.MockRepository;
import dev.mccue.resolve.maven.ModelParseException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveTest {
    @Test
    public void testResolve() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("org.clojure", "clojure", "1.11.0"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("org.clojure", "spec.alpha", "0.3.218"));
        expected.add(new Dependency("org.clojure", "core.specs.alpha", "0.2.62"));
        expected.add(new Dependency("org.clojure", "clojure", "1.11.0"));

        assertEquals(expected, r.listDependencies());
    }

    @Test
    public void testResolveUnrelatedDeps() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"))
                .addDependency(new Dependency("jresolve.test", "second.solo.dep", "1.1.1"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"));
        expected.add(new Dependency("jresolve.test", "second.solo.dep", "1.1.1"));

        assertEquals(expected, r.listDependencies());
    }

    @Test
    public void testResolveMatchingDeps() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"))
                .addDependency(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"));

        assertEquals(expected, r.listDependencies());
    }

    @Test
    public void testResolveConflictingVersions() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"))
                .addDependency(new Dependency("jresolve.test", "first.solo.dep", "1.2.0"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "first.solo.dep", "1.2.0"));

        assertEquals(expected, r.listDependencies());
    }

    @Test
    public void testResolveMatchingSubDeps() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.parent.of.new.dep", "1.12.3"))
                .addDependency(new Dependency("jresolve.test", "second.parent.of.new.dep", "1.10.2"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "second.parent.of.new.dep", "1.10.2"));
        expected.add(new Dependency("jresolve.test", "child.new.dep", "3.4.5"));
        expected.add(new Dependency("jresolve.test", "first.parent.of.new.dep", "1.12.3"));


        assertEquals(expected, r.listDependencies());
    }

    @Test
    public void testResolveConflictingVersionsSubDeps() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.parent.of.old.dep", "1.0.0"))
                .addDependency(new Dependency("jresolve.test", "first.parent.of.new.dep", "1.12.3"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "first.parent.of.old.dep", "1.0.0"));
        expected.add(new Dependency("jresolve.test", "child.new.dep", "3.4.5"));
        expected.add(new Dependency("jresolve.test", "first.parent.of.new.dep", "1.12.3"));


        assertEquals(expected, r.listDependencies());

        assertEquals(Set.of(
                new Dependency("jresolve.test", "first.parent.of.new.dep", "1.12.3"),
                new Dependency("jresolve.test", "first.parent.of.old.dep", "1.0.0"),
                new Dependency("jresolve.test", "child.new.dep", "3.4.5")
        ), mock.downloaded);
    }
}
