package dev.mccue.resolve.graph;

import dev.mccue.resolve.core.GroupAndArtifact;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;

import java.util.List;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/graph/ReverseModuleTree.scala")
public interface ReverseModuleTree {
    GroupAndArtifact module();

    String reconciledVersion();

    GroupAndArtifact dependsOnModule();

    String dependsOnVersion();

    String dependsOnReconciledVersion();

    boolean excludedDependsOn();

    List<ReverseModuleTree> dependees();
}
