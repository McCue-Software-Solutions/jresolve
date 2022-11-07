package dev.mccue.resolve.core;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static dev.mccue.resolve.core.DependencyGraph.Interdependency.*;

public final class DependencyGraph {
    private final Map<DependencyVertex, List<DependencyVertex>> deps = new HashMap<>();

     public enum Interdependency {
        TwoSubOfOne,
        OneSubOfTwo,
        NoRelation
    }

    /**
     * This method takes in a DependencyVertex and returns a list of its subdependencies
     * @param dep
     * @return list of subdependencies
     */
    List<DependencyVertex> getSubDependencies(DependencyVertex dep) {
        return deps.get(dep);
    }

    /**
     * This method takes in a DependencyVertex and adds it to the graph
     * @param dep
     */
    void addDependency(DependencyVertex dep) {
        deps.putIfAbsent(dep, new ArrayList<>());
    }

    /**
     * This method takes in a DependencyVertex and removes it from the graph
     * @param dep
     */
    void removeDependency(DependencyVertex dep) {
        deps.remove(dep);
        removeSubDependency(dep);
    }

    /**
     * This method takes in a primary DependencyVertex and a secondary
     * DependencyVertex. The secondary is added as a subdependency of the primary
     * @param dep
     * @param subDep
     */
    void addSubDependency(DependencyVertex dep, DependencyVertex subDep) {
        if(!deps.containsKey(dep)) {
            addDependency(dep);
        }

        if(!deps.containsKey(subDep)) {
            addDependency(subDep);
        }

        deps.get(dep).add(subDep);
    }

    /**
     * This method takes in a primary DependencyVertex and a secondary
     * DependencyVertex. The secondary is removed as a subdependency of the primary
     * @param dep
     * @param subDep
     */
    void removeSubDependency(DependencyVertex dep, DependencyVertex subDep) {
        deps.get(dep).remove(subDep);
    }

    /**
     * This method takes in a DependencyVertex and removes it from all
     * subdependency lists
     * @param subDep
     */
    void removeSubDependency(DependencyVertex subDep) {
        deps.values().removeAll(List.of(subDep));
    }

    /**
     * This method returns a boolean indicating whether the provided
     * DependencyVertex already exists in the graph
     * @param dep
     * @return
     */
    public boolean hasDependency(DependencyVertex dep) {
        return deps.containsKey(dep);
    }

    /**
     * This method wth produce a result indicating whether or not a dependency relationship
     * exists between the two dependencies
     * @param dep1 assumed as the parent dependency
     * @param dep2 assumed as the sub dependency
     * @return 1 if dep2 is a subdependency of dep1, -1 if dep1 is a subdependency of dep2,
     * 0 if neither are related
     */
    public Interdependency hasInterdependency(DependencyVertex dep1, DependencyVertex dep2) {
        if(deps.get(dep1).contains(dep2)) {
            return TwoSubOfOne;
        }

        if(deps.get(dep2).contains(dep1)) {
            return OneSubOfTwo;
        }

        return NoRelation;
    }

    /**
     * This method returns the number of dependencies in the graph
     * @return number of dependencies
     */
    public int getDependencyCount() {
        return deps.keySet().size();
    }

    /**
     * This method returns the number of subdependencies in the graph
     * @return number of subdependencies
     */
    public int getInterdependenciesCount() {
        int count = 0;
        for (DependencyVertex v : deps.keySet()) {
            count += deps.get(v).size();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(DependencyVertex  v: deps.keySet()) {
            builder.append(v.toString()).append(" \033[1;37m").append("requires").append("\033[0m { ");
            for(DependencyVertex w : deps.get(v)) {
                builder.append(w.toString()).append(" , ");
            }
            builder.append(" }\n");
        }

        return (builder.toString());
    }
}
