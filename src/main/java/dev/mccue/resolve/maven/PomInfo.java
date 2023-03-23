package dev.mccue.resolve.maven;

import dev.mccue.resolve.api.Repository;
import dev.mccue.resolve.core.*;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;
import dev.mccue.resolve.doc.MavenSpecific;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L228-L268")
public record PomInfo(
        Library module,
        String version,

        List<Tuple2<Configuration, Dependency>> dependencies,

        Map<Configuration, List<Configuration>> configurations,

        @MavenSpecific
        Optional<Tuple2<Library, String>> parent,

        @MavenSpecific
        List<Tuple2<Configuration, Dependency>> dependencyManagement,

        @MavenSpecific
        Map<String, String> properties,

        @MavenSpecific
        List<Profile> profiles,

        @MavenSpecific
        Optional<Versions> versions,

        @MavenSpecific
        Optional<SnapshotVersioning> snapshotVersioning,

        @MavenSpecific
        Optional<Type> packagingOpt,

        @MavenSpecific
        boolean relocated,

        @MavenSpecific
        Optional<String> actualVersionOpt,

        @MavenSpecific
        List<Tuple2<Configuration, Publication>> publications
) {
    public Tuple2<Library, String> moduleVersion() {
        return new Tuple2<>(module, version);
    }

    public Project toProject(Repository repository) {
        Map<String, String> properties = new HashMap<>();
        parent.ifPresent(parent -> {
            try {
                var parentProject = PomParser.parsePom(repository.getPom(new Dependency(parent.first(), parent.second()))).toProject(repository);
                dependencies().addAll(parentProject.dependencies());
                properties.putAll(this.properties);
                properties.putAll(parentProject.properties());
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (ModelParseException e) {
                throw new RuntimeException(e);
            }
            //combine projects
        });
        var project = new Project(module, version, dependencies, configurations, dependencyManagement,
                properties, profiles, versions, snapshotVersioning, packagingOpt,
                relocated, actualVersionOpt, publications);
        return project;
    }
}
