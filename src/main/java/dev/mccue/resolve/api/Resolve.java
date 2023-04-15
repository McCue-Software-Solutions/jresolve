package dev.mccue.resolve.api;

import dev.mccue.resolve.core.*;
import dev.mccue.resolve.maven.MavenRepository;
import dev.mccue.resolve.maven.ModelParseException;
import dev.mccue.resolve.maven.PomParser;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;


public class Resolve {
    private DependencyGraph dependencies;
    private Repository repository;

    public Resolve(Repository repository) {
        this.repository = repository;
        dependencies = new DependencyGraph(this.repository);
    }

    public Resolve addDependency(Dependency dep) throws ModelParseException, SAXException {
        dependencies.addDependency(dep);
        return this;
    }

    public Resolve addDependency(Dependency dep, Exclusions exclusions) throws ModelParseException, SAXException {
        dependencies.addDependency(dep, exclusions);
        return this;
    }

    public void run() {
        var d = dependencies.listDependencies();
        for (Dependency dep : d) {
            System.out.println(dep);
        }

        System.out.println(dependencies);
    }

    public ArrayList<Dependency> listDependencies() {
        return dependencies.listDependencies();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException, ModelParseException {
        var r = new Resolve(new MavenRepository())
                .addDependency(new Dependency("org.clojure", "clojure", "1.11.0"));
        r.run();
    }

}
