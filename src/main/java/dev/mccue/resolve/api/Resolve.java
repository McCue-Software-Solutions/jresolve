package dev.mccue.resolve.api;

import java.util.ArrayList;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.ExecutionContext;
import dev.mccue.resolve.maven.MavenCentralDownloader;

public class Resolve {
    private ArrayList<Dependency> dependencies;
    private ArrayList<Repository> repositories; //unused for now
    private Cache cache;

    public Resolve() {
        dependencies = new ArrayList<>();

        repositories = new ArrayList<>();
        //repositories.add(Repositories.mavenCentral or something like that)
        //TODO just to get everything working
    }

    public void addDependency(String dep) {
        dependencies.add(Dependency.parse(dep));
    }

//    public Resolution run(ExecutionContext ec) { //might not even need this Execution Context stuff TODO
//        return new Resolution();
//        
//    }

    public Resolution run() {
    //    ExecutionContext ec = cache.ec();
        for (var dependency : dependencies) {
            var downloader = new MavenCentralDownloader(dependency.module().organization(), dependency.module().name(), dependency.version());
            //downloader.get(".pom", Classifier.EMPTY, TODO path-from-cache);
            //downloader.get(".jar", Classifier.EMPTY, TODO path-from-cache);
            //downloader.get(".something else", Classifier.EMPTY, TODO path-from-cache);
        }
        return new Resolution();
    }

  
}
