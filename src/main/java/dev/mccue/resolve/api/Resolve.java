package dev.mccue.resolve.api;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Configuration;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;
import dev.mccue.resolve.maven.MavenRepository;
import dev.mccue.resolve.maven.PomParser;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


public class Resolve {
    private ArrayList<Dependency> dependencies;
    private ArrayList<Repository> repositories; //unused for now

    private static final String MAVEN_BASE_URL = "https://repo.maven.apache.org/maven2/";
    private MavenRepository maven;

    public Resolve() {
        dependencies = new ArrayList<>();
        repositories = new ArrayList<>();
        maven = new MavenRepository();
    }

    public Resolve addDependency(Dependency dep) {
        dependencies.add(dep);
        return this;
    }

    public void run() throws IOException, InterruptedException, SAXException {
        recursiveAddDependencies(dependencies);

        for (Dependency dependency : dependencies) {
            maven.download(dependency, Extension.POM, Classifier.EMPTY);
            maven.download(dependency, Extension.JAR, Classifier.EMPTY);
        }
    }

    private void recursiveAddDependencies(ArrayList<Dependency> recursiveDependencies) throws IOException, InterruptedException, SAXException {
        var newDependencies = new ArrayList<Dependency>();
        for (Dependency dep : recursiveDependencies) {
            for (Dependency found : getDependentPoms(dep)) {
                var alreadySeen = dependencies.stream()
                        .anyMatch(dependency1 ->
                                dependency1.library().equals(found.library()) &&
                                        dependency1.version().equals(found.version())
                        );
                if (!alreadySeen) {
                    newDependencies.add(found);
                }
            }
        }
        dependencies.addAll(newDependencies);

        if (!newDependencies.isEmpty()) {
            recursiveAddDependencies(newDependencies);
        }
    }

    public ArrayList<Dependency> getDependentPoms(Dependency dependency) throws IOException, InterruptedException, SAXException {
        var project = PomParser.parsePom(maven.getPom(dependency));

        var foundDependencies = new ArrayList<Dependency>();
        for (Tuple2<Configuration, Dependency> dep : project.dependencies()) {
            if (dep.first() == Configuration.EMPTY) {
                foundDependencies.add(dep.second());
            }
        }
        return foundDependencies;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        var r = new Resolve()
                .addDependency(new Dependency("org.clojure", "clojure", "1.11.0"));
        r.run();
    }

}
