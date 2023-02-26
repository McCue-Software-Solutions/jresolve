package dev.mccue.resolve.cache;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public enum OSLabel {
    LINUX,
    MAC_OS,
    WINDOWS;

    public static Optional<OSLabel> current() {
        String operatingSystem = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (operatingSystem.contains("nux")) {
            return Optional.of(LINUX);
        } else if (operatingSystem.contains("mac")) {
            return Optional.of(MAC_OS);
        } else if (operatingSystem.contains("win")) {
            return Optional.of(WINDOWS);
        } else {
            return Optional.empty();
        }
    }

    public String cachePath() {
        return switch (this) {
            case LINUX -> System.getProperty("user.home") + "/.cache/jresolve";
            case MAC_OS -> System.getProperty("user.home") + "/Library/Caches/JResolve";
            case WINDOWS -> System.getenv("APPDATA") + "\\JResolve\\Cache";
        };
    }

    public static Path cachePath(String url) {
        return Path.of(
                current()
                        .orElseThrow(() -> new RuntimeException("Operating System could not be determined."))
                        .cachePath(),
                url.split("(://|/)+", -1)
        );
    }
}
