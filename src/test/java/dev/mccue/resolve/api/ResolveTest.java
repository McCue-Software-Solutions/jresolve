package dev.mccue.resolve.api;

import dev.mccue.resolve.core.*;
import dev.mccue.resolve.maven.MockRepository;
import dev.mccue.resolve.maven.ModelParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.EnumSource;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolveTest {

    public boolean resolveTestCompareHelper(ArrayList<Dependency> expected, ArrayList<Dependency> actual) {
        return ((expected.size() == actual.size()) && (expected.containsAll(actual) && actual.containsAll(expected)));
    }
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

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
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

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
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

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
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

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
    }

    @Test
    public void testResolveMatchingSubDeps() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.parent.dep", "1.12.3"))
                .addDependency(new Dependency("jresolve.test", "second.parent.dep", "1.10.2"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "first.parent.dep", "1.12.3"));
        expected.add(new Dependency("jresolve.test", "second.parent.dep", "1.10.2"));
        expected.add(new Dependency("jresolve.test", "child.dep", "3.4.5"));
        expected.add(new Dependency("jresolve.test", "second.leaf.dep", "3.3.3"));


        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
    }

    @Test
    public void testResolveConflictingVersionsSubDeps() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "first.parent.of.old.dep", "1.0.0"))
                .addDependency(new Dependency("jresolve.test", "first.parent.dep", "1.12.3"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "first.parent.of.old.dep", "1.0.0"));
        expected.add(new Dependency("jresolve.test", "first.parent.dep", "1.12.3"));
        expected.add(new Dependency("jresolve.test", "child.dep", "3.4.5"));
        expected.add(new Dependency("jresolve.test", "second.leaf.dep", "3.3.3"));

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
    }

    @Test
    public void testResolveWithSingleExclusion() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        List<Exclusion> test_exclusions = new ArrayList<Exclusion>()
        test_exclusions.add(new Exclusion(new GroupId("jresolve.test"), new ArtifactId("first.leaf.dep")));
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "single.exclusion", "1.0.1"), Exclusions.of(test_exclusions));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "single.exclusion", "1.0.1"));

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
    }

    @Test
    public void testResolveWithPartialExclusion() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var dep1 = new Dependency("jresolve.test", "conflict.exclusion", "1.0.0");
        System.out.println("Exclusions: " + dep1.exclusions());
        var r = new Resolve(mock)
                .addDependency(dep1);
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "conflict.exclusion", "1.0.0"));
        expected.add(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"));

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
    }

    @Test
    public void testResolveWithConflictingExclusions() throws SAXException, ModelParseException {
        var mock = new MockRepository("graph1");
        var r = new Resolve(mock)
                .addDependency(new Dependency("jresolve.test", "conflict.exclusion", "1.0.0"))
                .addDependency(new Dependency("jresolve.test", "conflict.exclusion", "2.0.0"));
        r.run();

        ArrayList<Dependency> expected = new ArrayList<Dependency>();
        expected.add(new Dependency("jresolve.test", "conflict.exclusion", "2.0.0"));
        expected.add(new Dependency("jresolve.test", "first.solo.dep", "1.0.0"));
        expected.add(new Dependency("jresolve.test", "first.leaf.dep", "3.1.0"));

        assertTrue(resolveTestCompareHelper(expected, r.listDependencies()));
    }
}
