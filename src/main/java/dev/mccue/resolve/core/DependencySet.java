package dev.mccue.resolve.core;

import java.util.Map;
import java.util.Set;

public class DependencySet {
    private final Set<Dependency> set;
    private Map<Dependency, Sets<Dependency>> grouped;

    public static DependencySet empty() {
        return new DependencySet();
    }

    private DependencySet() {
        set = Set.of(); 
        grouped = Map.of();
    }

    public Set<Dependency> getSet() {
        return this.set;
    }
    
    private record Sets<T> (
        Map<Integer, Set<T>> required,
        Map<T, Set<T>> children,
        Map<T, T> parents
    ) {


        public static <T> Sets<T> empty() {
            return new Sets<T>(Map.of(), Map.of(), Map.of());
        }
    }
}

