package dev.mccue.resolve.core;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class JResolvePaths {
    private JResolvePaths() {
        throw new Error();
    }

    private static volatile File cacheDir = null;
    private static final Object cacheDirLock = new Object();

    private static String findCacheDir() throws IOException {
        String path = System.getenv("JRESOLVE_CACHE");

        if (path == null) {
            path = System.getProperty("jresolve.cache");
        }
        if (path != null) {
            return path;
        } //else {
//            TODO: Missing implementation of XDG base directory as fall-back
//        }
        return null;
    }

    public static File cacheDirectory() throws IOException {
        if (cacheDir == null) {
            synchronized (cacheDirLock) {
                if (cacheDir == null) {
                    cacheDir = new File(Objects.requireNonNull(findCacheDir())).getAbsoluteFile();
                }
            }
        }

        return cacheDir;
    }
}
