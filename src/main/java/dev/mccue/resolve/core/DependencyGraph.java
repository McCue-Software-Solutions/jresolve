package dev.mccue.resolve.core;

import dev.mccue.resolve.api.Repository;
import dev.mccue.resolve.maven.ModelParseException;
import dev.mccue.resolve.maven.PomParser;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static dev.mccue.resolve.core.DependencyGraph.Interdependency.*;

public final class DependencyGraph {
    private final Map<Library, List<Dependency>> deps = new HashMap<>();

    private Repository repository;

    public DependencyGraph(Repository repository) {
        this.repository = repository;
    }

    public enum Interdependency {
        TwoSubOfOne,
        OneSubOfTwo,
        NoRelation
    }

    public ArrayList<Dependency> listDependencies() {
        ArrayList<Dependency> fullDependencyList = new ArrayList<>();

        for (Library key : deps.keySet()) {
            fullDependencyList.add(deps.get(key).get(0));
        }

        return fullDependencyList;
    }

    /**
     * This method takes in a Dependency and returns a list of its subdependencies
     * @param dep
     * @return list of subdependencies
     */
    List<Dependency> getSubDependencies(Dependency dep) {
        return deps.get(dep.getLibrary());
    }

    /**
     * This method takes in a Dependency and adds it to the graph
     * @param dep
     */
    public void addDependency(Dependency dep) throws ModelParseException, SAXException {
        deps.putIfAbsent(dep.getLibrary(), new ArrayList<>());
        deps.get(dep.getLibrary()).add(dep);

        var dependent = getDependentPoms(dep);
        for (var dependentDep : dependent) {
            this.addSubDependency(dep, dependentDep);
        }
    }

    public ArrayList<Dependency> getDependentPoms(Dependency dependency) throws SAXException, ModelParseException {
        var project = PomParser.parsePom(this.repository.getPom(dependency));

        var foundDependencies = new ArrayList<Dependency>();
        for (Tuple2<Configuration, Dependency> dep : project.dependencies()) {
            if (dep.first() == Configuration.EMPTY) {
                foundDependencies.add(dep.second());
            }
        }
        return foundDependencies;
    }


    /**
     * This method takes in a Dependency and removes it from the graph
     * @param dep
     */
    public void removeDependency(Dependency dep) {
        deps.remove(dep.getLibrary());
        removeSubDependency(dep);
    }

    /**
     * This method takes in a primary Dependency and a secondary
     * Dependency. The secondary is added as a subdependency of the primary
     * @param dep
     * @param subDep
     */
    public void addSubDependency(Dependency dep, Dependency subDep) throws ModelParseException, SAXException {
        if(!deps.containsKey(dep.getLibrary())) {
            addDependency(dep);
        }

        if(!deps.containsKey(subDep.getLibrary())) {
            addDependency(subDep);
        }

        deps.get(dep.getLibrary()).add(subDep);
    }

    /**
     * This method takes in a primary Dependency and a secondary
     * Dependency. The secondary is removed as a subdependency of the primary
     * @param dep
     * @param subDep
     */
    public void removeSubDependency(Dependency dep, Dependency subDep) {
        deps.get(dep.getLibrary()).remove(subDep);
    }

    /**
     * This method takes in a Dependency and removes it from all
     * subdependency lists
     * @param subDep
     */
    public void removeSubDependency(Dependency subDep) {
        deps.values().removeAll(List.of(subDep));
    }

    /**
     * This method returns a boolean indicating whether the provided
     * Dependency already exists in the graph
     * @param dep
     * @return
     */
    public boolean hasDependency(Dependency dep) {
        return deps.containsKey(dep.getLibrary());
    }

    /**
     * This method wth produce a result indicating whether or not a dependency relationship
     * exists between the two dependencies
     * @param dep1 assumed as the parent dependency
     * @param dep2 assumed as the sub dependency
     * @return 1 if dep2 is a subdependency of dep1, -1 if dep1 is a subdependency of dep2,
     * 0 if neither are related
     */
    public Interdependency hasInterdependency(Dependency dep1, Dependency dep2) {
        if(deps.get(dep1.getLibrary()).contains(dep2)) {
            return TwoSubOfOne;
        }

        if(deps.get(dep2.getLibrary()).contains(dep1)) {
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
        for (Library v : deps.keySet()) {
            count += deps.get(v).size();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(Library  v: deps.keySet()) {
            builder.append(v.toString()).append(" \033[1;37m").append("requires").append("\033[0m { ");
            for(Dependency w : deps.get(v)) {
                builder.append(w.toString()).append(" , ");
            }
            builder.append(" }\n");
        }

        return (builder.toString());
    }
}
