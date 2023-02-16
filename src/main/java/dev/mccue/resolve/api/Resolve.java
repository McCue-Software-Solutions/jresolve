package dev.mccue.resolve.api;

import java.util.ArrayList;

import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.ExecutionContext;

public class Resolve {
    private ArrayList<Dependency> dependencies;
    private ArrayList<Repository> repositories;
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

    public Resolution run(ExecutionContext ec) { //might not even need this Execution Context stuff TODO
        return new Resolution();
        
    }

    public Resolution run() {
        ExecutionContext ec = cache.ec();
        return new Resolution();
    }

  
}
