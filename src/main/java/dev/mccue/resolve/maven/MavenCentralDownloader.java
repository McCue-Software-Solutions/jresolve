package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Extension;
import dev.mccue.resolve.core.ModuleName;
import dev.mccue.resolve.core.Organization;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Creates an object from a specified path at maven central. Allows for the downloading of POMS and Jars.
 */
public final class MavenCentralDownloader {

    private static final String MAVEN_BASE_URL = "https://repo.maven.apache.org/maven2/";

    private final String dependencyBaseURL;
    private final Organization organization;
    private final ModuleName moduleName;
    private final String version;

    /**
     * Constructor for dev.mccue.resolve.maven.MavenCentralDownloader
     * Checks the connection for successful 200 response
     *
     */
    public MavenCentralDownloader(Organization organization, ModuleName moduleName, String version) {
        this.organization = organization;
        this.moduleName = moduleName;
        this.version = version;

        var organizationUrlFragment = Arrays.stream(organization.value().split("\\."))
                .map(part -> URLEncoder.encode(part, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));

        this.dependencyBaseURL = MAVEN_BASE_URL +
                organizationUrlFragment + "/" +
                URLEncoder.encode(moduleName.value(), StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(version, StandardCharsets.UTF_8) + "/";
    }

    /**
     * Downloads the POM file locally and stores it at the specified path
     */
    public void get(Extension extension, Classifier classifier, Path path) throws IOException, InterruptedException {
        Files.createDirectories(path);
        var fileName = moduleName.value()
                + "-"
                + version
                + (classifier.isEmpty() ? "" : "-" + classifier.value() )
                + (extension.isEmpty() ? "" : "." + extension.value());

        var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(this.dependencyBaseURL + fileName))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofFile(
                Path.of( path.toString(), fileName)
        ));

        if (response.statusCode() != 200) {
            throw new IOException("Bad response code: " + response.statusCode());
        }
    }

    public static void main(String[] args) throws Exception {
        var u = new MavenCentralDownloader(
                new Organization("HTTPClient"),
                new ModuleName("HTTPClient"),
                "0.3-3"
        );
        u.get(Extension.POM, Classifier.EMPTY, Path.of("./downloads/http/"));
        u.get(Extension.JAR, Classifier.EMPTY, Path.of("./downloads/http/"));

        var u2 = new MavenCentralDownloader(
                new Organization("com.agile4j"),
                new ModuleName("agile4j-feed-builder"),
                "1.0.3"
        );

        u2.get(Extension.JAR, Classifier.JAVADOC, Path.of("./downloads/http/"));
    }

}