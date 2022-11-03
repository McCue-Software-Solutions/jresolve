package dev.mccue.resolve.core;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public final class CacheHandler {
    public enum OSLabel {
        Linux, MacOS, Windows
    }
    private static OSLabel currentOS;
    private static String CACHE_PATH;

    public CacheHandler() {
        /* This only handles the default cache path and will need to
        *  be modified when new features are implemented (e.g. CLI --cache option)
        */
        checkOS();
        switch (currentOS){
            case Linux:
                CACHE_PATH = System.getProperty("user.home") + "/.cache/jresolve";
                break;
            case MacOS:
                CACHE_PATH = System.getProperty("user.home") + "/Library/Caches/JResolve";
                break;
            case Windows:
                CACHE_PATH = System.getenv("APPDATA") + "\\JResolve\\Cache";
                break;
            case null:
                throw new IllegalArgumentException("Operating System could not be determined.");
            case default:
                break;
        }
    }

    public void cacheAdd(String url) throws Exception {
        String[] parsedURL = URLParser(url);
        StringBuilder path;
        if (currentOS != OSLabel.Windows) {
            path = new StringBuilder(CACHE_PATH + "/" + parsedURL[0] + "/");
            for(int i = 1; i < parsedURL.length-1; i++) {
                path.append(parsedURL[i]).append("/");
            }
        } else {
            path = new StringBuilder(CACHE_PATH + "\\" + parsedURL[0] + "\\");
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

    public String[] URLParser(String url) {
        return url.split("(://|/)+", -1);
    }

    public static void checkOS() {
        if (currentOS == null) {
            String operatingSystem = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if (operatingSystem.contains("nux")) {
                currentOS = OSLabel.Linux;
            } else if (operatingSystem.contains("mac")) {
                currentOS = OSLabel.MacOS;
            } else if (operatingSystem.contains("win")) {
                currentOS = OSLabel.Windows;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CacheHandler cache = new CacheHandler();
        cache.cacheAdd("https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.7/scala-library-2.12.7.jar");
    }
}
