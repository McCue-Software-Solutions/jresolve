package dev.mccue.resolve.cache;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CacheHandler {
    private static OS os = OS.get();

    public void cacheAdd(String url) throws Exception {

        Path path = os.location.resolve(URLParser(url));
        Files.createDirectories(path.getParent());

        try ( var readableByteChannel = Channels.newChannel(new URL(url).openStream());
              var fileOutputStream = new FileOutputStream(path.toString());
        ) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    //Not really needed anymore
    public Path URLParser(String url) {
        return Paths.get(String.join("/", url.split("(://|/)+", -1)));
    }

    //Simply to test loading functionality
    public static void main(String[] args) throws Exception {
        CacheHandler cache = new CacheHandler();
        cache.cacheAdd("https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.7/scala-library-2.12.7.jar");
    }
}
