package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.*;
import dev.mccue.resolve.core.Library;
import dev.mccue.resolve.util.Tuple2;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PomParserTest {

    @Test
    public void parseBasicPOM() throws SAXException {
        var basicPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                                
                    <groupId>dev.mccue</groupId>
                    <artifactId>resolve</artifactId>
                    <version>0.0.1</version>
                    <packaging>jar</packaging>
                                
                    <properties>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    </properties>
                                
                    <dependencies>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-api</artifactId>
                            <version>5.9.0</version>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-params</artifactId>
                            <version>5.9.0</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                                
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.8.1</version>
                                <configuration>
                                    <source>19</source>
                                    <target>19</target>
                                    <compilerArgs>--enable-preview</compilerArgs>
                                </configuration>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-source-plugin</artifactId>
                                <version>3.0.1</version>
                                <executions>
                                    <execution>
                                        <id>attach-sources</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-javadoc-plugin</artifactId>
                                <version>3.2.0</version>
                                <executions>
                                    <execution>
                                        <id>attach-javadocs</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-surefire-plugin</artifactId>
                                <version>3.0.0-M7</version>
                                <configuration>
                                    <argLine>@{argLine} --enable-preview</argLine>
                                </configuration>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.jacoco</groupId>
                                <artifactId>jacoco-maven-plugin</artifactId>
                                <version>0.8.8</version>
                                <executions>
                                    <execution>
                                        <id>jacoco-initialize</id>
                                        <goals>
                                            <goal>prepare-agent</goal>
                                        </goals>
                                    </execution>
                                    <execution>
                                        <id>jacoco-site</id>
                                        <phase>test</phase>
                                        <goals>
                                            <goal>report</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """;

        try {
            var project = PomParser.parsePom(new ByteArrayInputStream(basicPom.getBytes(StandardCharsets.UTF_8)));

            assertEquals(new GroupId("dev.mccue"), project.module().groupId());
            assertEquals(new ArtifactId("resolve"), project.module().artifactId());
            assertEquals("0.0.1", project.version());
            assertEquals(Map.of("project.build.sourceEncoding", "UTF-8"), project.properties());
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
    public void variableReplacementTest() throws ParserConfigurationException, SAXException, IOException {
        var pomParser = new PomParser();
        var basicPom = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                                
                    <groupId>dev.mccue</groupId>
                    <artifactId>resolve</artifactId>
                    <version>0.0.1</version>
                    <packaging>jar</packaging>
                                
                    <properties>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <junit.version.one>5.9.0</junit.version.one>
                        <junit.version.two>5.9</junit.version.two>
                    </properties>
                                
                    <dependencies>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-api</artifactId>
                            <version>${junit.version.one}</version>
                            <scope>test</scope>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-params</artifactId>
                            <version>${junit.version.two}.0</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                                
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.8.1</version>
                                <configuration>
                                    <source>19</source>
                                    <target>19</target>
                                    <compilerArgs>--enable-preview</compilerArgs>
                                </configuration>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-source-plugin</artifactId>
                                <version>3.0.1</version>
                                <executions>
                                    <execution>
                                        <id>attach-sources</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-javadoc-plugin</artifactId>
                                <version>3.2.0</version>
                                <executions>
                                    <execution>
                                        <id>attach-javadocs</id>
                                        <goals>
                                            <goal>jar</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-surefire-plugin</artifactId>
                                <version>3.0.0-M7</version>
                                <configuration>
                                    <argLine>@{argLine} --enable-preview</argLine>
                                </configuration>
                            </plugin>
                                
                            <plugin>
                                <groupId>org.jacoco</groupId>
                                <artifactId>jacoco-maven-plugin</artifactId>
                                <version>0.8.8</version>
                                <executions>
                                    <execution>
                                        <id>jacoco-initialize</id>
                                        <goals>
                                            <goal>prepare-agent</goal>
                                        </goals>
                                    </execution>
                                    <execution>
                                        <id>jacoco-site</id>
                                        <phase>test</phase>
                                        <goals>
                                            <goal>report</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """;
        var factory = SAXParserFactory.newDefaultInstance();
        var saxParser = factory.newSAXParser();
        saxParser.parse(
                new ByteArrayInputStream(basicPom.getBytes(StandardCharsets.UTF_8)),
                pomParser
        );

        try {
            var project = pomParser.project();

            assertEquals(new GroupId("dev.mccue"), project.module().groupId());
            assertEquals(new ArtifactId("resolve"), project.module().artifactId());
            assertEquals("0.0.1", project.version());
            assertEquals(Map.of( "junit.version.two", "5.9", "junit.version.one", "5.9.0", "project.build.sourceEncoding", "UTF-8"), project.properties());
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
