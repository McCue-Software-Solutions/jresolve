package dev.mccue.resolve.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;

import dev.mccue.resolve.util.Tuple2;
import dev.mccue.resolve.util.Tuple3;

public record Resolution(
    List<Dependency> rootDependencies,
    DependencySet dependencySet,
    //forceVersions
    List<Dependency> conflicts,
    Map<Resolution.ModuleVersion, Tuple2<ArtifactSource, Project>> projectCache,
    //errorCache
    Map<Dependency, List<Dependency>> finalDependenciesCache,
    //filter
    //reconciliation
    //osInfo
    //jdkVersion
    //userActivations
    //mapDependencies
    //extraProperties
    //forceProperties
    Configuration defaultConfiguration
) {
    private interface ProjectCache {
        public Optional<Project> createProject(Tuple2<Module, String> moduleVersion);
    }

    private static List<Dependency> finalDependencies(
        Dependency from,
        Project project,
        Configuration defaultConfiguration,
        ProjectCache projectCache
    ) {
        return List.of();
        //TODO this is horrendus
    }

    private record ModuleVersion(Module module, String version) {}

    public Resolution(List<Dependency> rootDependencies) {
        this(rootDependencies, DependencySet.empty(), List.of(), Map.of(), Map.of(), Configuration.DEFAULT_COMPILE);
    }

    private static List<Tuple2<String, String>> projectProperties(Project project) {
        final var packaging = project.packagingOpt().orElse(Type.JAR);

        var properties0 = new ArrayList<>(project.properties());

        properties0.addAll(List.of(
            //all of these are not required by all artifacts, are they required by maven artifacts?
            new Tuple2<> ("pom.groupId", project.module().organization().value()),
            new Tuple2<> ("pom.artifactId", project.module().name().value()),
            new Tuple2<> ("pom.version", project.actualVersion()),
            new Tuple2<> ("groupId", project.module().organization().value()),
            new Tuple2<> ("artifactId", project.module().name().value()),
            new Tuple2<> ("version", project.actualVersion()),
            new Tuple2<> ("project.groupId", project.module().organization().value()),
            new Tuple2<> ("project.artifactId", project.module().name().value()),
            new Tuple2<> ("project.version", project.actualVersion()),
            new Tuple2<> ("project.packaging", packaging.value())
        ));

        return substitute(properties0);
    }

    private static boolean hasProps(String string) {
        var ok = false;
        var index = 0;

        while (index < string.length() && !ok) {
            var dolIndex = index;
            while (dolIndex < string.length() && string.charAt(dolIndex) != '$') {
                dolIndex++;
            }
            index = dolIndex;

            if (dolIndex < string.length() - 2 && string.charAt(dolIndex + 1) == '{') {
                var endIndex = dolIndex + 2;
                while (endIndex < string.length() && string.charAt(endIndex) != '}') {
                    endIndex++;
                }
                if (endIndex < string.length()) {
                    //assert s.charAt(endIndex) == '}'
                    ok = true;
                }
            }

            if (!ok && index < string.length()) {
                //asser s.charAt(index) == '$'
                index++;
            }
        }

        return ok;
    }

    private static String substituteProps(String s, Map<String, String> properties) {
        return substituteProps(s, properties, false);
    }

    private static String substituteProps(String string, Map<String, String> properties, boolean trim) {
        StringBuilder builder = null;
        var index = 0;

        while (index < string.length()) {
            var dolIndex = index;
            while (dolIndex < string.length() && string.charAt(dolIndex) != '$') {
                dolIndex += 1;
            }
            if (index != 0 || dolIndex < string.length()) {
                if (builder == null) {
                    builder = new StringBuilder(string.length() + 32);
                }
                builder.append(string, index, dolIndex);
            }
            index = dolIndex;

            String name = null;
            if (dolIndex < string.length() - 2 && string.charAt(dolIndex + 1) == '{') {
                var endIndex = dolIndex + 2;
                while (endIndex < string.length() && string.charAt(endIndex) != '}') {
                    endIndex += 1;
                }
                if (endIndex < string.length()) {
                    //assert s.charAt(endIndex) == '}' ?
                    name = string.substring(dolIndex + 2, endIndex);
                }
            }

            if (name == null) {
                if (index < string.length()) {
                    //assert(string.charAt(index) == '$')?
                    builder.append('$');
                    index += 1;
                }
            } else {
                index = index + 2 + name.length() + 1; //== endIndex + 1?
                if (properties.containsKey(name)) {
                    builder.append(string, dolIndex, index);
                } else {
                    final var value = properties.get(name);
                    final var v0 = trim ? value.trim() : value;
                    builder.append(v0);
                }
            }
        }

        if (builder == null) {
            return string;
        } else {
            return builder.toString();
        }
    }

    public static List<Tuple2<String, String>> substitute(List<Tuple2<String, String>> properties0) {
        var didSubstitutions = false;

        var done = Map.<String, String>of();

        List<Tuple2<String, String>> res = new ArrayList<>();
        for (var t : properties0) {
            var k = t.first();
            var v = t.second();
            final var result = substituteProps(v, done);
            if (!didSubstitutions) {
                didSubstitutions = result != v;
            }
            res.add(new Tuple2<> (k, result));
        }

        if (didSubstitutions) {
            return substitute(res);
        } else {
            return res;
        }

    }

    public static Project withFinalProperties(Project project) {
        return project.withProperties(projectProperties(project));
    }

    private static Dependency fallbackConfigIfNecessaary(Dependency dependency, Set<Configuration> set) {
        //Parse.withFallbackConfig(dependency.configuration) TODO
        return null;
    }

    //End static remove

    private Set<Dependency> dependencies() {
        return dependencySet.getSet();
    }

    private Resolution withDependencySet(DependencySet set) {
        return new Resolution(
            this.rootDependencies,
            set,
            this.conflicts,
            this.projectCache,
            this.finalDependenciesCache,
            defaultConfiguration
        );
    }

    public Resolution withDependencies(Set<Dependency> dependencies) {
        return withDependencySet(dependencySet.setValues(dependencies));
    }

    private Set<Tuple2<Module, String>> missingFromCache() {
        final var modules =
                dependencies().stream().map(s -> s.moduleVersion()).collect(Collectors.toSet()); //ew
        //TODO the scala here seems like the dependency resolution algorithm

        return modules;
    }

    private Map<Dependency, List<Dependency>> finalDependenciesCache0() {
        return new ConcurrentHashMap<>();
    }

    private List<Dependency> findFinalDependencies(Dependency dependency) {
        if (dependency.transitive()) {
            final var deps = finalDependenciesCache.getOrDefault(dependency, finalDependenciesCache0().get(dependency));

            if (deps == null) {
                if (projectCache.containsKey(dependency.moduleVersion())) {
                    final var project = projectCache.get(dependency.moduleVersion()).second();
                    final var resolution0 = finalDependencies(
                        dependency,
                        project,
                        defaultConfiguration,
                        key -> Optional.of(projectCache.get(key).second())); //TODO filter
                    final List<Dependency> result = List.of();
                    finalDependenciesCache0().put(dependency, result);
                    return result;
                } else {
                    return null;
                }

            } else {
                return deps;
            }
        } else {
            return null;
        }
    }

    public List<Dependency> dependenciesOf(Dependency dependency) {
        return dependenciesOf(dependency, false);
    }

    public List<Dependency> dependenciesOf(Dependency dependency, boolean withRetainedVersions) {
        return dependenciesOf(dependency, withRetainedVersions, false);
    }

    private Set<Configuration> configsOf(Dependency dependency) {
    //     return projectCache TODO this
    //         .get(dependency.moduleVersion())
    //         .map()
        return Set.of();
    }

    public List<Dependency> dependenciesOf(
        Dependency dependency,
        boolean withRetainedVersions, 
        boolean withFallbackConfig
    ) {
        final var dependency0 = withRetainedVersions ? dependency.withVersion(retainedVersions().getOrDefault(dependency.module(), dependency.version())) : dependency;
        if (withFallbackConfig) {
            return List.of(Resolution.fallbackConfigIfNecessaary(dependency0, configsOf(dependency0)));
        } else {
            return List.of(dependency0);
        }

    }

    private Tuple3<List<Dependency>, List<Dependency>, Map<Module, String>> nextDependenciesAndConflicts() { //TOOD implement this
        return new Tuple3(
            List.of(),
            List.of(),
            Map.of()
        );
    }

    public Map<Module, String> retainedVersions() {
        return nextDependenciesAndConflicts().third();
    }

    public Resolution addToProejctCache(List<Tuple2<Resolution.ModuleVersion, Tuple2<ArtifactSource, Project>>> projects) { //This needs some different type
        return null;
    }

    private List<Dependency> transitiveDependencies() {
        return dependencySet.minimizedSet().stream()
            .filter(dependency -> !conflicts.contains(dependency))
            .map(this::findFinalDependencies)
            .flatMap(list -> list.stream())
            .toList();
    }

    public boolean isDone() {
        // TODO return missingFromCache().isEmpty() && isFixPoint();
        return isFixPoint();
    }

    private boolean isFixPoint() { // TODO
        return false;
    }
}

