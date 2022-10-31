package dev.mccue.resolve;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Creates an object from a specified path at maven central. Allows for the downloading of POMS and Jars.
 */
public class MavenCentralDownloader {

    private final String mavenBaseURL = "https://repo.maven.apache.org/maven2/";
    private final String pomExtension = ".pom";
    private final String jarExtension = ".jar";

    private final String dependencyBaseURL;
    private final String group;
    private final String name;
    private final String version;
    private final String path;

    /**
     * Constructor for dev.mccue.resolve.MavenCentralDownloader
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
        this.dependencyBaseURL = mavenBaseURL +
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
    public void getPOM() throws IOException {
        Files.createDirectories(Paths.get(this.path));
        var fileName = name + "-" + version + pomExtension;
        var url = new URL(this.dependencyBaseURL + fileName);
        var readableByteChannel = Channels.newChannel(url.openStream());
        var fileOutputStream = new FileOutputStream(this.path + fileName + ".xml");
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        readableByteChannel.close();
    }

    /**
     * Downloads the Jar file locally and stores it at the specified path
     *
     * @throws IOException
     */
    public void getJar() throws IOException {
        Files.createDirectories(Paths.get(this.path));
        var fileName = name + "-" + version + jarExtension;
        var url = new URL(this.dependencyBaseURL + fileName);
        var readableByteChannel = Channels.newChannel(url.openStream());
        var fileOutputStream = new FileOutputStream(this.path + fileName);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        readableByteChannel.close();
    }

    public static void main(String[] args) throws Exception {
        var u = new MavenCentralDownloader("junit", "junit", "4.9", "./downloads/junit/");
        u.getPOM();
        u.getJar();
    }

}