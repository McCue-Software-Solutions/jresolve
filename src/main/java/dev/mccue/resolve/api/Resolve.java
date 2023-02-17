package dev.mccue.resolve.api;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;
import dev.mccue.resolve.maven.MavenCentralDownloader;

import java.util.ArrayList;

public class Resolve {
    private ArrayList<Dependency> dependencies;
    private ArrayList<Repository> repositories; //unused for now

    public Resolve() {
        dependencies = new ArrayList<>();
        repositories = new ArrayList<>();
    }

    public Resolve addDependency(Dependency dep) {
        dependencies.add(dep);
        return this;
    }

    public void run() {
        for (Dependency dependency : dependencies) {
            var downloader = new MavenCentralDownloader();
            downloader.get(dependency, Extension.POM, Classifier.EMPTY);
            downloader.get(dependency, Extension.JAR, Classifier.EMPTY);
            downloader.get(dependency, Extension.JAR, Classifier.SOURCES);
            downloader.get(dependency, Extension.JAR, Classifier.JAVADOC);
            downloader.get(dependency, Extension.POM, Classifier.JAVADOC);
        }
    }

    public static void main(String[] args) {
        var r = new Resolve()
                .addDependency(new Dependency("org.clojure", "clojure", "1.11.0"));
        r.run();
    }

}
