package dev.mccue.resolve.api;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.core.Extension;

import java.io.InputStream;

public interface Repository {
    String download(Dependency dependency, Extension extension, Classifier classifier);

    InputStream getPom(Dependency dependency);
}
