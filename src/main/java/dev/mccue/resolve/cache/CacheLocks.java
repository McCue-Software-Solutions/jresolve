package dev.mccue.resolve.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class CacheLocks {
    private static final ConcurrentHashMap<String, Object> interned = new ConcurrentHashMap<>();

    public <T> T withLock(Path path, Callable<T> callable) throws Exception {
        Object procLock = lockInit(path);

        synchronized (procLock) {
            File lockFile = new File(path.toFile(), ".lock");
            Files.createDirectories(lockFile.toPath().getParent());
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(lockFile);
                FileLock lock = null;

                try {
                    lock = out.getChannel().lock();

                    try {
                        return callable.call();
                    } finally {
                        lock.release();
                        lock = null;
                        out.close();
                        out = null;
                        lockFile.delete();
                    }
                } finally {
                    if (lock != null) {
                        lock.release();
                    }
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    private static Object lockInit(Path cachePath) {
        String lockKey = "jresolve-lock-" + cachePath.toString();
        Object existing = interned.get(lockKey);

        if (existing == null) {
            String internedKey = lockKey.intern();
            interned.putIfAbsent(internedKey, internedKey);
            existing = internedKey;
        }

        return existing;
    }

}
