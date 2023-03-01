package dev.mccue.resolve.maven;

import dev.mccue.resolve.cache.OSLabel;
import dev.mccue.resolve.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Creates an object from a specified path at maven central. Allows for the downloading of POMS and Jars.
 */
public final class MavenRepository {
    private final String base_url;

    /**
     * Constructor for dev.mccue.resolve.maven.MavenCentralDownloader
     * Checks the connection for successful 200 response
     */
    public MavenRepository(String base_url) {
        this.base_url = base_url;
    }

    public MavenRepository() {
        // Maven Central by default
        this("https://repo.maven.apache.org/maven2/");
    }

    private <T> HttpResponse<T> get(URI uri, HttpResponse.BodyHandler<T> handler) {
        try {
            var client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            var headRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .HEAD()
                    .build();
            var headResponse = client.send(headRequest, HttpResponse.BodyHandlers.ofString());
            if (headResponse.statusCode() != 200) {
                throw new IOException("Bad response code: " + headResponse.statusCode());
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();

            var response = client.send(request, handler);

            if (response.statusCode() != 200) {
                throw new IOException("Bad response code: " + response.statusCode());
            }

            return response;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private URI uriOf(Dependency dependency, Extension extension, Classifier classifier) {
        var groupId = dependency.library().groupId();
        var artifactId = dependency.library().artifactId();
        var version = dependency.version();

        var groupUrlFragment = Arrays.stream(groupId.value().split("\\."))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));

        var dependencyBaseURL = base_url +
                groupUrlFragment + "/" +
                URLEncoder.encode(artifactId.value(), StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(version, StandardCharsets.UTF_8) + "/";

        var fileName = artifactId.value()
                + "-"
                + version
                + (classifier.isEmpty() ? "" : "-" + classifier.value())
                + (extension.isEmpty() ? "" : "." + extension.value());
        return URI.create(dependencyBaseURL + fileName);
    }

    /**
     * Downloads the POM file locally and stores it at the specified path
     */
    public void download(Dependency dependency, Extension extension, Classifier classifier) {
        var uri = uriOf(dependency, extension, classifier);
        var path = OSLabel.cachePath(uri.toString());
        try {
            Files.createDirectories(path.getParent());
            get(uri, HttpResponse.BodyHandlers.ofFile(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public InputStream getPom(Dependency dependency) {
        var uri = uriOf(dependency, Extension.POM, Classifier.EMPTY);
        return get(uri, HttpResponse.BodyHandlers.ofInputStream()).body();
    }
}