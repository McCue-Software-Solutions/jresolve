package dev.mccue.resolve.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import dev.mccue.resolve.core.ArtifactId;
import dev.mccue.resolve.core.Configuration;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.GroupId;
import dev.mccue.resolve.core.Library;
import dev.mccue.resolve.core.Type;
import dev.mccue.resolve.util.Tuple2;

public class PomInfoTest {

        @Test
        public void ParseBasicParentPom() throws SAXException {
                try {
                        var repository = new MockRepository("pomInfo");
                        var project = PomParser
                                        .parsePom(repository.getPom(new Dependency("dev.test",
                                                        "test.pom", "0.0.1")))
                                        .toProject(repository);

                        assertEquals(new GroupId("dev.test"), project.module().groupId());
                        assertEquals(new ArtifactId("test.pom"), project.module().artifactId());
                        assertEquals(Map.of("project.build.sourceEncoding", "UTF-8", "test.version",
                                        "1.0.0", "junit.version.one", "5.9.0", "junit.version.two",
                                        "5.9"), project.properties());
                        assertEquals("0.0.1", project.version());
                        assertEquals(Optional.of(Type.JAR), project.packagingOpt());

                        assertEquals(List.of(new Tuple2<>(Configuration.TEST,
                                        new Dependency(new Library(new GroupId("org.junit.jupiter"),
                                                        new ArtifactId("junit-jupiter-api")),
                                                        "5.9.0")),
                                        new Tuple2<>(Configuration.TEST, new Dependency(new Library(
                                                        new GroupId("org.junit.jupiter"),
                                                        new ArtifactId("junit-jupiter-params")),
                                                        "5.9.0"))),
                                        project.dependencies());
                } catch (ModelParseException e) {
                }
        }

        @Test
        public void ParseSecondParentPom() throws SAXException {
                try {
                        var repository = new MockRepository("pomInfo");
                        var project = PomParser.parsePom(repository
                                        .getPom(new Dependency("dev.test", "child", "0.0.1")))
                                        .toProject(repository);

                        assertEquals(Map.of("project.build.sourceEncoding", "UTF-8", "test.version",
                                        "1.0.0", "junit.version.one", "5.9.0", "junit.version.two",
                                        "5.9"), project.properties());
                        assertEquals(List.of(new Tuple2<>(Configuration.TEST,
                                        new Dependency(new Library(new GroupId("dev.test"),
                                                        new ArtifactId("fake.dependency")),
                                                        "1.0.0")),
                                        new Tuple2<>(Configuration.TEST, new Dependency(new Library(
                                                        new GroupId("org.junit.jupiter"),
                                                        new ArtifactId("junit-jupiter-api")),
                                                        "5.9.0")),
                                        new Tuple2<>(Configuration.TEST, new Dependency(new Library(
                                                        new GroupId("org.junit.jupiter"),
                                                        new ArtifactId("junit-jupiter-params")),
                                                        "5.9.0"))),
                                        project.dependencies());
                } catch (ModelParseException e) {
                }
        }

        @Test
        public void dependencyManagementTest() throws SAXException {
                var repository = new MockRepository("pomInfo");
                try {
                        var project = PomParser
                                        .parsePom(repository.getPom(new Dependency("dev.test",
                                                        "test.pom", "0.0.1")))
                                        .toProject(repository);

                        assertEquals(new GroupId("dev.test"), project.module().groupId());
                        assertEquals(new ArtifactId("test.pom"), project.module().artifactId());
                        assertEquals(Map.of("project.build.sourceEncoding", "UTF-8", "test.version",
                                        "1.0.0", "junit.version.one", "5.9.0", "junit.version.two",
                                        "5.9"), project.properties());
                        assertEquals("0.0.1", project.version());
                        assertEquals(Optional.of(Type.JAR), project.packagingOpt());

                        assertEquals(List.of(new Tuple2<>(Configuration.TEST,
                                        new Dependency(new Library(new GroupId("org.junit.jupiter"),
                                                        new ArtifactId("junit-jupiter-api")),
                                                        "5.9.0")),
                                        new Tuple2<>(Configuration.TEST, new Dependency(new Library(
                                                        new GroupId("org.junit.jupiter"),
                                                        new ArtifactId("junit-jupiter-params")),
                                                        "5.9.0"))),
                                        project.dependencies());
                } catch (ModelParseException e) {
                }
        }
}
