package dev.mccue.resolve.cache;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public final class CacheDefaults {
    //Defaults to be added include cachePolicies, checksums, credentials, logger, etc...
    public enum OSLabel {
        Linux, MacOS, Windows
    }
    public static String cacheLocation;

    public static String archiveLocation;

    public CacheDefaults(OSLabel os) {
        switch (os){
            case Linux:
                cacheLocation = System.getProperty("user.home") + "/.cache/jresolve";
                break;
            case MacOS:
                cacheLocation = System.getProperty("user.home") + "/Library/Caches/JResolve";
                break;
            case Windows:
                cacheLocation = System.getenv("APPDATA") + "\\JResolve\\Cache";
                break;
            case null:
                throw new IllegalArgumentException("Operating System could not be determined.");
            case default:
                break;
        }
    }
}
