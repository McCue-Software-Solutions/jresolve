package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.*;
import dev.mccue.resolve.core.Library;
import dev.mccue.resolve.util.Tuple2;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PomParserTest {

    @Test
    public void parseBasicPOM() throws SAXException {
        var repository = new MockRepository("parserTest");
        try {
            var project = PomParser.parsePom(repository.getPom(new Dependency("dev.mccue", "resolve", "0.0.1")));

            assertEquals(new GroupId("dev.mccue"), project.module().groupId());
            assertEquals(new ArtifactId("resolve"), project.module().artifactId());
            assertEquals("0.0.1", project.version());
            assertEquals(Map.of( "junit.version.two", "5.9", "junit.version.one", "5.9.0", "pomInfo.build.sourceEncoding", "UTF-8"), project.properties());
            assertEquals(Optional.of(Type.JAR), project.packagingOpt());

            assertEquals(List.of(
                    new Tuple2<>(Configuration.TEST, new Dependency(
                            new Library(
                                    new GroupId("org.junit.jupiter"),
                                    new ArtifactId("junit-jupiter-api")
                            ),
                            "5.9.0"
                    )),
                    new Tuple2<>(Configuration.TEST, new Dependency(
                            new Library(
                                    new GroupId("org.junit.jupiter"),
                                    new ArtifactId("junit-jupiter-params")
                            ),
                            "5.9.0"
                    ))
            ), project.dependencies());
        } catch (ModelParseException e) { }
    }

    @Test
    public void variableReplacementTest() throws SAXException {
        var repository = new MockRepository("parserTest");

        try {
            var project = PomParser.parsePom(repository.getPom(new Dependency("dev.mccue", "resolve", "0.0.2")));
            assertEquals(new GroupId("dev.mccue"), project.module().groupId());
            assertEquals(new ArtifactId("resolve"), project.module().artifactId());
            assertEquals("0.0.2", project.version());
            assertEquals(Map.of( "junit.version.two", "5.9", "junit.version.one", "5.9.0", "pomInfo.build.sourceEncoding", "UTF-8"), project.properties());
            assertEquals(Optional.of(Type.JAR), project.packagingOpt());

            assertEquals(List.of(
                    new Tuple2<>(Configuration.TEST, new Dependency(
                            new Library(
                                    new GroupId("org.junit.jupiter"),
                                    new ArtifactId("junit-jupiter-api")
                            ),
                            "5.9.0"
                    )),
                    new Tuple2<>(Configuration.TEST, new Dependency(
                            new Library(
                                    new GroupId("org.junit.jupiter"),
                                    new ArtifactId("junit-jupiter-params")
                            ),
                            "5.9.0"
                    ))
            ), project.dependencies());
        } catch (ModelParseException e) { 
        }
    }
}
