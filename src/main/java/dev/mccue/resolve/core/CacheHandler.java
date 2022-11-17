package dev.mccue.resolve.core;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheHandler {
    public enum OSLabel {
        LINUX, MACOS, WINDOWS
    }
    private static final OSLabel CURRENT_OS;
    private static final String CACHE_PATH;

    static {
        /* This only handles the default cache path and will need to
        *  be modified when new features are implemented (e.g. CLI --cache option)
        */
        String operatingSystem = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        OSLabel currentOS = null;
        if (operatingSystem.contains("nux")) {
            currentOS = OSLabel.LINUX;
        } else if (operatingSystem.contains("mac")) {
            currentOS = OSLabel.MACOS;
        } else if (operatingSystem.contains("win")) {
            currentOS = OSLabel.WINDOWS;
        }
        CURRENT_OS = currentOS;

        CACHE_PATH = switch (currentOS){
            case LINUX ->
                System.getProperty("user.home") + "/.cache/jresolve";
            case MACOS ->
                System.getProperty("user.home") + "/Library/Caches/JResolve";
            case WINDOWS ->
                System.getenv("APPDATA") + "\\JResolve\\Cache";
            case null ->
                throw new IllegalArgumentException("Operating System could not be determined.");
        };
    }

    public String[] URLSplitter(String url) {
        return url.split("(://|/)+", -1);
    }

    public static File localFile(String url, File currCache, String username, boolean needsCached) throws MalformedURLException, URISyntaxException {
        if (url.startsWith("file:/") && !needsCached) {
            try {
                return Paths.get(new URI(url)).toFile();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        String[] urlSearch = url.split(":", 2);
        if (urlSearch.length < 2) {
            throw new MalformedURLException("Missing protocol in: " + url);
        }

        String pre = urlSearch[0];
        String post = urlSearch[1];
        String rest = "";

        if (post.startsWith("///")) {
            rest = post.substring(3);
        } else if (post.startsWith("/")) {
            rest = post.substring(1);
        } else {
            throw new MalformedURLException("Missing absolute path in: " + url);
        }

        while (rest.startsWith("/")) {
            rest = rest.substring(1);
        }

        String user = "";
        if (username != null) {
            user = username + "@";
        }

        URI finalURI = new URI(pre, "/", user, rest);
        return new File(currCache, finalURI.toASCIIString());
    }

    public static Path lockPath(Path path) {
        return path.getParent().resolve(path.getFileName().toString() + ".lock");
    }

    public static File lockFile(File file) {
        return new File(file.getParentFile(), file.getName() + ".lock");
    }

    public static File defaultCache() throws IOException {
        return JResolvePaths.cacheDirectory();
    }

    private static final ConcurrentHashMap<String, Object> cacheStringInterns = new ConcurrentHashMap<>();
    private static Object lockWith(Path path) {
        String key = "jresolve-lock-" + path.toString();
        if (cacheStringInterns.contains(key)) {
            return cacheStringInterns.get(key);
        } else {
            String newIntern = key.intern();
            cacheStringInterns.putIfAbsent(newIntern, newIntern);
            return newIntern;
        }
    }

    public static <V> V structurePathLock(Path path, Callable<V> call) throws Exception {
        Object procLock = lockPath(path);

        synchronized (procLock) {
            Path locked = path.resolve(".structure.lock");
            try {
                Files.createDirectories(path);
            } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(path)) {
                    throw e;
                }
            }
            FileChannel chan = null;
            
            try {
                chan = FileChannel.open( locked, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE);

                FileLock lock = null;
                try {
                    lock = chan.lock();
                    
                    try {
                        return call.call();
                    } finally {
                        lock.release();
                        chan.close();
                    }
                } finally {
                    if (lock != null) {
                        lock.release();
                    }
                }
            } finally {
                if (chan != null) {
                    chan.close();
                }
            }
        }
    }

    public static <V> V structureFileLock (File file, Callable<V> call) throws Exception {
        Path filePath = file.toPath();
        Object procLock = lockPath(filePath);

        synchronized (procLock) {
            File locked = new File(file, ".structure.lock");
            try {
                Files.createDirectories(filePath.getParent());
            } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(filePath.getParent())) {
                    throw e;
                }
            }

            try (FileOutputStream stream = new FileOutputStream(locked)) {

                FileLock lock = null;
                try {
                    lock = stream.getChannel().lock();

                    try {
                        return call.call();
                    } finally {
                        lock.release();
                        stream.close();
                        locked.delete();
                    }
                } finally {
                    if (lock != null) {
                        lock.release();
                    }
                }
            }
        }
    }
}
