package dev.mccue.resolve.util;

import java.util.Optional;
import java.util.Map;

record Artifact (
    String url, 
    Map<String, String> checksumUrls,
    Map<String, Artifact> extra,
    boolean changing,
    boolean optional,
    Optional<Authentication> authentication
) {
    public Artifact(String url) {
        this(url, Map.of(), Map.of(), false, false, Optional.empty());
    }

    public Artifact withChanging(boolean changing) {
        return new Artifact(this.url, this.checksumUrls, this.extra, changing, this.optional, this.authentication);
    }

    public static Artifact fromUrl(String url) {
        String url0;
        boolean changing;
        if (url.endsWith("?changing")) {
            url0 = removeSuffix(url, "?changing");
            changing = true;
        } else if (url.endsWith("?changing=true")) {
            url0 = removeSuffix(url, "?changing=true");
            changing = true;
        } else if (url.endsWith("?changing=false")) {
            url0 = removeSuffix(url, "?changing=false");
            changing = false;
        } else {
            url0 = url;
            changing = false;
        }
        return new Artifact(url0).withChanging(changing);
    }

    private static String removeSuffix(String s, String suffix) {
        if (s != null && s.endsWith(suffix)) {
            return s.split(suffix)[0];
        }
        return s;
    }
    
}
