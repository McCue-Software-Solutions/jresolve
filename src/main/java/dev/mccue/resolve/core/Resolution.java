package dev.mccue.resolve.core;

import java.util.List;
import java.util.Set;

public record Resolution(
    List<Dependency> rootDependencies,
    DependencySet dependencySet
) {

    public Resolution(List<Dependency> rootDependencies) {
        this(rootDependencies, DependencySet.empty());
    }
    
    private Set<Dependency> dependencies() {
        return dependencySet.getSet();
    }

    public boolean isDone() {
        // TODO return missingFromCache().isEmpty() && isFixPoint();
        return isFixPoint();
    }

    private boolean isFixPoint() { //TODO
        return false;
    }
}
