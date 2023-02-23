package dev.mccue.resolve.api;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Configuration;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;
import dev.mccue.resolve.maven.MavenCentralDownloader;
import dev.mccue.resolve.maven.PomParser;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Resolve {
    private ArrayList<Dependency> dependencies;
    private ArrayList<Repository> repositories; //unused for now

    private static final String MAVEN_BASE_URL = "https://repo.maven.apache.org/maven2/";

    public Resolve(List<Dependency> dependencies) {
        this.dependencies = new ArrayList<>(dependencies);
        repositories = new ArrayList<>();
    }

//    public Resolve addDependency(Dependency dep) {
//        dependencies.add(dep);
//        return this;
//    }

    public void run() throws IOException, ParserConfigurationException, InterruptedException, SAXException {
        for (var dep : new ArrayList<>(dependencies)) {
            getDependenciesRecursive(dep);
        }
        for (Dependency dependency : dependencies) {
            System.out.println(dependency);
            var downloader = new MavenCentralDownloader();
            try {
                downloader.get(dependency, Extension.POM, Classifier.EMPTY);
            } catch (UncheckedIOException e) {
            }
            try {
                downloader.get(dependency, Extension.JAR, Classifier.EMPTY);
            } catch (UncheckedIOException e) {
            }
            try {
                downloader.get(dependency, Extension.JAR, Classifier.SOURCES);
            } catch (UncheckedIOException e) {
            }
            try {
                downloader.get(dependency, Extension.JAR, Classifier.JAVADOC);
            } catch (UncheckedIOException e) {
            }
            try {
                downloader.get(dependency, Extension.POM, Classifier.JAVADOC);
            } catch (UncheckedIOException e) {
            }
        }
    }

    private void getDependenciesRecursive(Dependency dep) throws IOException, ParserConfigurationException, InterruptedException, SAXException {
        for (var subDep : getDependentPoms(dep)) {
            var alreadySeen = dependencies.stream().anyMatch(dependency1 ->
                    dependency1.library().equals(subDep.library()) &&
                    dependency1.version().equals(subDep.version())
                );
            if (alreadySeen) {
                continue;
            }
            dependencies.add(subDep);
            getDependenciesRecursive(subDep);
        }
    }

    private ArrayList<Dependency> getDependentPoms(Dependency dependency) throws IOException, InterruptedException, SAXException, ParserConfigurationException {
        var groupId = dependency.library().groupId();
        var artifactId = dependency.library().artifactId();
        var version = dependency.version();

        var groupUrlFragment = Arrays.stream(groupId.value().split("\\."))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));

        var dependencyBaseURL = MAVEN_BASE_URL +
                groupUrlFragment + "/" +
                URLEncoder.encode(artifactId.value(), StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(version, StandardCharsets.UTF_8) + "/";

        var fileName = artifactId.value()
                + "-"
                + version
                + "." + Extension.POM.value();

        var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        var uri = URI.create(dependencyBaseURL + fileName);

        var headRequest = HttpRequest.newBuilder()
                .uri(uri)
                .HEAD()
                .build();
        var headResponse = client.send(headRequest, HttpResponse.BodyHandlers.ofString());
        if (headResponse.statusCode() != 200) {
            throw new IOException("Bad response code: " + headResponse.statusCode());
        }

        var request = HttpRequest.newBuilder()
                .uri(uri)
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());


        var pom = new PomParser();
        var factory = SAXParserFactory.newDefaultInstance();
        var saxParser = factory.newSAXParser();
        saxParser.parse(response.body(), pom);

        var project = pom.project();

        var foundDependencies = new ArrayList<Dependency>();
        for (Tuple2<Configuration, Dependency> dep : project.dependencies()) {
            if (dep.first() == Configuration.EMPTY) {
                foundDependencies.add(dep.second());
            }
        }
        return foundDependencies;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException {
        var r = new Resolve(List.of(new Dependency("org.clojure", "clojure", "1.11.0")));
        r.run();
    }

}
