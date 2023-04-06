package dev.mccue.resolve.core;

import java.util.List;

public record DependencyNode(
        Dependency dependency,

        List<Dependency> childrenNodes
) {

    public DependencyNode(Dependency dependency, List<Dependency> childrenNodes) {
        this.dependency = dependency;
        this.childrenNodes = childrenNodes;
    }
}
