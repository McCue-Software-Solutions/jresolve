package dev.mccue.resolve.core;

import java.util.Objects;

public record DependencyVertex (String artifactID, String groupID, String version){
    public DependencyVertex(String artifactID, String groupID, String version) {
        this.artifactID = Objects.requireNonNull(artifactID, "artifactID must not be null");
        this.groupID = Objects.requireNonNull(groupID, "groupID must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }
        DependencyVertex vertex = (DependencyVertex) other;
        return Objects.equals(artifactID, vertex.artifactID)
                && Objects.equals(groupID, vertex.groupID)
                && Objects.equals(version, vertex.version);
    }

    @Override
    public int hashCode(){
        return this.artifactID.hashCode() + this.groupID.hashCode() + this.version.hashCode();
    }

    @Override
    public String toString() {
        return "[NAME: " + this.artifactID + " GROUPID: " + this.groupID + " VERSION: " + this.version + "]";
    }
}
