package dev.mccue.resolve.graph;

import dev.mccue.resolve.api.Resolution;
import dev.mccue.resolve.core.Dependency;

import java.util.*;
import java.util.stream.Collectors;

public interface DependencyTree {
/*
    static List<DependencyTree> of(
            Resolution resolution,
            List<Dependency> roots,
            boolean withExclusions
    ) {
        List<Dependency> initialRoots;
        if (!roots.isEmpty()) {
            initialRoots = resolution.rootDependencies;
        } else {
            initialRoots = roots;
        }

        return initialRoots.stream().<DependencyTree>map(dep -> new Node(dep, false, resolution, withExclusions)).toList();
    }

    record Node(
            Dependency dependency,
            Boolean excluded,
            Resolution resolution,
            Boolean withExclusions
    ) implements DependencyTree {
        String reconciledVersion() {
            if (resolution.reconciledVersion.dependency.module() != null) {
                return resolution.reconciledVersion.dependency.module().toString();
            } else {
                return resolution.reconciledVersion.dependency.version();
            }
        }

        String retainedVersion() {
            if (resolution.retainedVersion.dependency.module() != null) {
                return resolution.retainedVersion.dependency.module().toString();
            } else {
                return resolution.retainedVersion.dependency.version();
            }
        }

        List<DependencyTree> children() {
            if (excluded) {
                return null;
            } else {
                var dep0 = dependency.withVersion(retainedVersion());

                List<Dependency> dependencies = resolution.dependenciesOf(dep0, false)
                        .stream()
                        .sorted(
                                Comparator.comparing((Dependency trDep) -> trDep.module().organization())
                                        .thenComparing(trDep -> trDep.module().name())
                                        .thenComparing(Dependency::version)
                        )
                        .collect(Collectors.toList());

                List dependencies0 = dependencies.stream().map(Dependency::moduleVersion).toList();

                var excluded = resolution.dependenciesOf(dep0.withExclusions(List.of(), false))
                        .stream()
                        .sorted(
                                Comparator.comparing((Dependency trDep) -> trDep.module().organization())
                                        .thenComparing(trDep -> trDep.module().name())
                                        .thenComparing(Dependency::version)
                        )
                        .filter(trDep -> !dependencies0.contains(trDep.moduleVersion()))
                        .map(trDep -> Node.apply(trDep, true, resolution, withExclusions))
                        .collect(Collectors.toList());

                List dependenciesList = dependencies.stream().map(d -> Node.apply(d, false, resolution, withExclusions))
                        .collect(Collectors.toList());

                if (withExclusions) {
                    dependenciesList.addAll(excluded);
                }

                return dependenciesList;
            }
        }
    }*/
}
