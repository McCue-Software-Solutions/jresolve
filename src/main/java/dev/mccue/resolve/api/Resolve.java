package dev.mccue.resolve.api;

import dev.mccue.resolve.core.*;
import dev.mccue.resolve.maven.MavenRepository;
import dev.mccue.resolve.maven.ModelParseException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
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

    public void run() {
        var d = dependencies.listDependencies();
        for (Dependency dep : d) {
            System.out.println(dep);
        }

        System.out.println(dependencies);
    }

    public void downloadJARs(String pathToSaveFile, String fileName, String fileExtension) {
        var d = dependencies.listDependencies();
        var builder = new StringBuilder();
        for (Dependency dep : d) {
            builder.append(repository.download(dep, Extension.JAR, Classifier.EMPTY)).append(";\n");
        }
        try {
            var file = new File(pathToSaveFile, fileName + "." + fileExtension);
            var writer = new FileWriter(file, false);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Dependency> listDependencies() {
        return dependencies.listDependencies();
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException, ModelParseException {
        var r = new Resolve(new MavenRepository())
                .addDependency(new Dependency("junit", "junit", "4.9"));
        r.run();
    }

}
