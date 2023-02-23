package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ArtifactIdTest {
    @Test
    @DisplayName("Module artifactId doesn't transform value")
    public void emptyModuleNameHasEmptyValue() {
        assertEquals("abc", new ArtifactId("abc").value());
        assertEquals("ABC", new ArtifactId("ABC").value());
    }

    @Test
    @DisplayName("ModuleNames are lexicographically sortable")
    public void moduleNamesAreSortableByValue() {
        var moduleNames = new ArrayList<>(List.of(
                new ArtifactId("f"),
                new ArtifactId("e"),
                new ArtifactId("a"),
                new ArtifactId("b"),
                new ArtifactId("d"),
                new ArtifactId("c")
        ));

        Collections.sort(moduleNames);

        assertEquals(List.of(
                new ArtifactId("a"),
                new ArtifactId("b"),
                new ArtifactId("c"),
                new ArtifactId("d"),
                new ArtifactId("e"),
                new ArtifactId("f")
        ), moduleNames);
    }

    @Test
    @DisplayName("Cant make a ModuleName with a null value")
    public void cantMakeNullValuedModuleName() {
        assertThrows(
                NullPointerException.class,
                () -> new ArtifactId(null),
                "Should not be able to make a ModuleName with a null value."
        );
    }


    @Test
    @DisplayName("Can use map to update the value in a ModuleName")
    public void mapType() {
        assertEquals(
                new ArtifactId("ABC"),
                new ArtifactId("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapType() {
        assertThrows(
                NullPointerException.class,
                () -> new ArtifactId("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

}
