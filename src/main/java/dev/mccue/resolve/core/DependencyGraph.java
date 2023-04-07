package dev.mccue.resolve.core;

import dev.mccue.resolve.api.Repository;
import dev.mccue.resolve.maven.ModelParseException;
import dev.mccue.resolve.maven.PomParser;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;

import java.util.*;

public final class DependencyGraph {
    private final Map<Library, DependencyNode> deps = new HashMap<>();
    private final ArrayList<DependencyNode> exclusionDeps = new ArrayList<>();

    private Repository repository;

    public DependencyGraph(Repository repository) {
        this.repository = repository;
    }

    public ArrayList<Dependency> listDependencies() {
        ArrayList<Dependency> fullDependencyList = new ArrayList<>();

        for (Library key : deps.keySet()) {
            fullDependencyList.add(deps.get(key).dependency());
        }

        return fullDependencyList;
    }

    /**
     * This method takes in a Dependency and returns a list of its subdependencies
     *
     * @param dep
     * @return list of subdependencies
     */
    List<Dependency> getSubDependencies(Dependency dep) {
        return deps.get(dep.getLibrary()).childrenNodes();
    }

    private ArrayList<Dependency> getDependentPoms(Dependency dependency) throws SAXException, ModelParseException {
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
     * This method takes in a Dependency and adds it to the graph
     *
     * @param dep
     */
    public void addDependency(Dependency dep) throws ModelParseException, SAXException {
        addDependency(dep, Exclusions.of(Collections.emptySet()));
    }

    private void addDependency(Dependency dep, Exclusions exclusions) throws ModelParseException, SAXException {
        var node = new DependencyNode(dep, getDependentPoms(dep));

        if (exclusions.contains(dep)) {
            return;
        }

        var currentNode = deps.putIfAbsent(node.dependency().getLibrary(), node);   //null if absent, current node if exists
        if (currentNode != null) {
            if (currentNode.dependency().compareVersion(node.dependency()) < 0) {                                   //if newer version
                this.removeDependency(currentNode.dependency());                                                        //remove lower version
                deps.put(node.dependency().getLibrary(), node);                                                         //replace node with higher version
            } else if (currentNode.dependency().compareVersion(node.dependency()) == 0) {                           //else if same version
                if (currentNode.dependency().exclusions() != node.dependency().exclusions()) {                          //if different exclusions
                    var newExclusions = currentNode.dependency().exclusions().meet(node.dependency().exclusions());         //create intersection of exclusions
                    this.removeDependency(currentNode.dependency());                                                        //remove old dependency
                    var updatedDep = new Dependency(                                                                        //create updated dependency
                            node.dependency().library(),
                            node.dependency().version(),
                            node.dependency().configuration(),
                            newExclusions,
                            node.dependency().publication(),
                            node.dependency().optional(),
                            node.dependency().transitive()
                    );
                    node = new DependencyNode(updatedDep, getDependentPoms(dep));
                    deps.put(node.dependency().getLibrary(), node);                                                         //add updated dependency
                } else {
                    return;                                                                                         //else if older, return
                }
            }
        }
        var nextExclusion = exclusions.join(node.dependency().exclusions());
        for (var dependentDep : node.childrenNodes()) {
            this.addDependency(dependentDep, nextExclusion);
        }
    }

    /**
     * This method takes in a Dependency and removes it from the graph
     *
     * @param dep
     */
    public void removeDependency(Dependency dep) {
        var removedNode = deps.remove(dep.getLibrary());
        if (removedNode != null) {
            for (Dependency child : removedNode.childrenNodes()) {
                removeDependency(child);
            }
        }
    }

    /**
     * This method returns a boolean indicating whether the provided
     * Dependency already exists in the graph
     *
     * @param dep
     * @return
     */
    public boolean hasDependency(Dependency dep) {
        return deps.containsKey(dep.getLibrary());
    }

    /**
     * This method returns the number of dependencies in the graph
     *
     * @return number of dependencies
     */
    public int getDependencyCount() {
        return deps.keySet().size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int first = 1;

        for (Library v : deps.keySet()) {
            var dep = deps.get(v).dependency();
            builder.append(dep.library()).append(" ").append(dep.version()).append(" \033[1;37m").append("requires").append("\033[0m { ");
            for (Dependency w : deps.get(v).childrenNodes()) {
                if (first == 1) {
                    builder.append("[").append(w.library()).append(" ").append(w.version()).append("]");
                    first = 0;
                } else {
                    builder.append(" , ").append("[").append(w.library()).append(" ").append(w.version()).append("]");
                }
            }
            builder.append(" }\n");
            first = 1;
        }

        return (builder.toString());
    }
}
