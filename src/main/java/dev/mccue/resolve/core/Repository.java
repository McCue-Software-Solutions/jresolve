package dev.mccue.resolve.core;

import dev.mccue.resolve.maven.MavenRepository;

import java.util.List;

public interface Repository {
    static List<Repository> defaults() {
        return List.of(central());
    }

    static MavenRepository central() {
        return new MavenRepository("repo1.maven.org/maven2");
    }
}
