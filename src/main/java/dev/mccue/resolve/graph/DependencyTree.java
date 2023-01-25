package dev.mccue.resolve.graph;

import dev.mccue.resolve.core.Dependency;

import java.util.*;
import java.util.stream.Collectors;

public sealed interface DependencyTree {

    //TODO roots0 can be optionally null
    static List<DependencyTree> of(
            Resolution resolution,
            List<Dependency> roots,
            boolean withExclusions
    ) {
        //TODO Delete after you write code
        record Person(String name, int age) {
        }

        List<Person> persons = new ArrayList<>();
        persons.sort(Comparator.comparing(Person::age));
        persons.sort(Comparator.nullsFirst(Comparator.comparing(Person::name)).thenComparing(Comparator.comparing(Person::age)));

        Set<Integer> ages = Set.of();

        persons.stream()
                .sorted(Comparator.comparing(Person::age))
                .filter(person -> !ages.contains(person.age))
                .map(person -> new Object[]{person.age})
                .toList();


        List<Dependency> roots0;
        if (!roots.isEmpty()) {
            roots0 = resolution.rootDependencies;
        } else {
            roots0 = roots;
        }

        return roots0.stream().<DependencyTree>map(dep -> new Node(dep, false, resolution, withExclusions)).toList();
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
                        .sorted(Comparator.comparing((Dependency trDep) ->
                                trDep.module().organization()).thenComparing(trDep ->
                                trDep.module().name()).thenComparing(trDep -> trDep.version()))
                        .collect(Collectors.toList());

                List dependencies0 = dependencies.stream().map(Dependency::moduleVersion).collect(Collectors.toList());

                var excluded = resolution.dependenciesOf(dep0.withExclusions(Collections.emptyList(), false))
                        .stream()
                        .sorted(Comparator.comparing((Dependency trDep) ->
                                trDep.module().organization()).thenComparing(trDep ->
                                trDep.module().name()).thenComparing(trDep -> trDep.version()))
                        .filter(trDep ->
                                !dependencies0.contains(trDep.moduleVersion()))
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
    }
}
