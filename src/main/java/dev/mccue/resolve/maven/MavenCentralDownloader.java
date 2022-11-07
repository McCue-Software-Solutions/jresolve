package dev.mccue.resolve.maven;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Creates an object from a specified path at maven central. Allows for the downloading of POMS and Jars.
 */
public final class MavenCentralDownloader {

    private static final String MAVEN_BASE_URL = "https://repo.maven.apache.org/maven2/";
    private static final String POM_EXTENSION = ".pom";
    private static final String JAR_EXTENSION = ".jar";

    private final String dependencyBaseURL;
    private final String group;
    private final String name;
    private final String version;
    private final String path;

    /**
     * Constructor for dev.mccue.resolve.maven.MavenCentralDownloader
     * Checks the connection for successful 200 response
     *
     * @param group   the name of dependencies' group
     * @param name    the name of the dependency
     * @param version the version of the dependency
     */
    public MavenCentralDownloader(String group, String name, String version, String relativePath) throws Exception {
        this.group = group;
        this.name = name;
        this.version = version;
        this.path = relativePath;
        this.dependencyBaseURL = MAVEN_BASE_URL +
                URLEncoder.encode(group, StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(name, StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(version, StandardCharsets.UTF_8) + "/";

        var url = new URL(this.dependencyBaseURL);
        var conn = (HttpURLConnection) url.openConnection();
        conn.disconnect();
        if (conn.getResponseCode() != 200) {
            throw new Exception("Bad Response Code: " + conn.getResponseCode());
        }
    }

    /**
     * Downloads the POM file locally and stores it at the specified path
     *
     * @throws IOException
     */
    public void getPOM() throws IOException, InterruptedException {
        Files.createDirectories(Paths.get(this.path));
        var fileName = name + "-" + version + POM_EXTENSION;
        var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(this.dependencyBaseURL + fileName))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            try (var outputStream = new FileOutputStream(this.path + fileName + ".xml")) {
                outputStream.write(response.body());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Downloads the Jar file locally and stores it at the specified path
     *
     * @throws IOException
     */
    public void getJar() throws IOException, InterruptedException {
        Files.createDirectories(Paths.get(this.path));
        var fileName = name + "-" + version + JAR_EXTENSION;
        var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(this.dependencyBaseURL + fileName))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            try (var outputStream = new FileOutputStream(this.path + fileName)) {
                outputStream.write(response.body());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        var u = new MavenCentralDownloader("HTTPClient", "HTTPClient", "0.3-3", "./downloads/http/");
        u.getPOM();
        u.getJar();
    }

}