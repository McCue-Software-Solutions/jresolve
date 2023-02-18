package dev.mccue.resolve.cache;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public enum OS {
    Linux(System.getProperty("user.home") + "/.cache/jresolve"),
    MacOS(System.getProperty("user.home") + "/Library/Caches/JResolve"),
    Windows(System.getenv("APPDATA") + "\\JResolve\\Cache");

    Path location;
    Path archive;

    OS(String location) {
        this.location = Paths.get(location);
    }

    public static OS get() {
        String operatingSystem = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (operatingSystem.contains("nux")) {
            return OS.Linux;
        } else if (operatingSystem.contains("mac")) {
            return OS.MacOS;
        } else if (operatingSystem.contains("win")) {
            return OS.Windows;
        }
        return null;
    }
}
