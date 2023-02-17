package dev.mccue.resolve.maven;

import dev.mccue.resolve.cache.OSLabel;
import dev.mccue.resolve.core.*;

import java.io.IOException;
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
public final class MavenCentralDownloader {

    private static final String MAVEN_BASE_URL = "https://repo.maven.apache.org/maven2/";

    /**
     * Constructor for dev.mccue.resolve.maven.MavenCentralDownloader
     * Checks the connection for successful 200 response
     */
    public MavenCentralDownloader() {

    }

    /**
     * Downloads the POM file locally and stores it at the specified path
     */
    public void get(Dependency dependency, Extension extension, Classifier classifier) {
        try {
            var organization = dependency.module().organization();
            var moduleName = dependency.module().name();
            var version = dependency.version();

            var organizationUrlFragment = Arrays.stream(organization.value().split("\\."))
                    .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8))
                    .collect(Collectors.joining("/"));

            var dependencyBaseURL = MAVEN_BASE_URL +
                    organizationUrlFragment + "/" +
                    URLEncoder.encode(moduleName.value(), StandardCharsets.UTF_8) + "/" +
                    URLEncoder.encode(version, StandardCharsets.UTF_8) + "/";

            var fileName = moduleName.value()
                    + "-"
                    + version
                    + (classifier.isEmpty() ? "" : "-" + classifier.value())
                    + (extension.isEmpty() ? "" : "." + extension.value());

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

            var path = OSLabel.cachePath(uri.toString());
            Files.createDirectories(path.getParent());
            var response = client.send(request, HttpResponse.BodyHandlers.ofFile(path));

            if (response.statusCode() != 200) {
                throw new IOException("Bad response code: " + response.statusCode());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}