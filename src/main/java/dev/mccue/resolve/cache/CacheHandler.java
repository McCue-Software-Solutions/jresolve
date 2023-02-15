package dev.mccue.resolve.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import dev.mccue.resolve.cache.CacheDefaults.OSLabel;

public final class CacheHandler {
    private static OSLabel currentOS;
    private static String cacheLocation;

    public void cacheAdd(String url) throws Exception {
        new CacheDefaults(checkOS());
        cacheLocation = CacheDefaults.cacheLocation;

        String[] parsedURL = URLParser(url);
        StringBuilder path;
        if (currentOS != OSLabel.Windows) {
            path = new StringBuilder(cacheLocation + "/" + parsedURL[0] + "/");
            for(int i = 1; i < parsedURL.length-1; i++) {
                path.append(parsedURL[i]).append("/");
            }
        } else {
            path = new StringBuilder(cacheLocation + "\\" + parsedURL[0] + "\\");
            for(int i = 1; i < parsedURL.length-1; i++) {
                path.append(parsedURL[i]).append("\\");
            }
        }
        Files.createDirectories(Paths.get(path.toString()));

        URL downloadURL = new URL(url);
        try ( var readableByteChannel = Channels.newChannel(downloadURL.openStream());
              var fileOutputStream = new FileOutputStream(path + parsedURL[parsedURL.length-1]);
        ) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    public static OSLabel checkOS() {
        if (currentOS == null) {
            String operatingSystem = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if (operatingSystem.contains("nux")) {
                currentOS = CacheDefaults.OSLabel.Linux;
            } else if (operatingSystem.contains("mac")) {
                currentOS = CacheDefaults.OSLabel.MacOS;
            } else if (operatingSystem.contains("win")) {
                currentOS = CacheDefaults.OSLabel.Windows;
            }
        }

        return currentOS;
    }

    //Not really needed anymore
    public String[] URLParser(String url) {
        return url.split("(://|/)+", -1);
    }

    //Simply to test loading functionality
    public static void main(String[] args) throws Exception {
        CacheHandler cache = new CacheHandler();
        cache.cacheAdd("https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.7/scala-library-2.12.7.jar");
    }
}
