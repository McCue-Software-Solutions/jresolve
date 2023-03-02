package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class MockRepository implements Repository {
    public Set<Dependency> downloaded = new HashSet<>();
    private String base_path;

    public MockRepository(String data) {
        base_path = "src/test/java/data/" + data + "/";
    }

    @Override
    public void download(Dependency dependency, Extension extension, Classifier classifier) {
        if (extension.equals(Extension.POM)) {
            downloaded.add(dependency);
        }
    }

    @Override
    public InputStream getPom(Dependency dependency) {

        var artifactId = dependency.library().artifactId();
        var version = dependency.version();
        File pom = Path.of(base_path + artifactId.value()
                + "-"
                + version
                + ".pom").toFile();
        try {
            return new FileInputStream(pom);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
