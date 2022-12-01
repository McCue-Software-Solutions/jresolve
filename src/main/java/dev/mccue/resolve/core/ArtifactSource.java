package dev.mccue.resolve.core;

import java.util.List;
import java.util.Optional;

import dev.mccue.resolve.util.Artifact;
import dev.mccue.resolve.util.Tuple2;

public interface ArtifactSource {
    public List<Tuple2<Publication, Artifact>> artifacts(Dependency dependency, Project project, Optional<List<Classifier>> overrideClassifiers);
}