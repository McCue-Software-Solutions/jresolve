package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class GroupIdTest {
    @Test
    @DisplayName("Organization doesn't transform value")
    public void emptyModuleNameHasEmptyValue() {
        assertEquals("abc", new GroupId("abc").value());
        assertEquals("ABC", new GroupId("ABC").value());
    }

    @Test
    @DisplayName("Organizations are lexicographically sortable")
    public void organizationsAreSortableByValue() {
        var organizations = new ArrayList<>(List.of(
                new GroupId("f"),
                new GroupId("e"),
                new GroupId("a"),
                new GroupId("b"),
                new GroupId("d"),
                new GroupId("c")
        ));

        Collections.sort(organizations);

        assertEquals(List.of(
                new GroupId("a"),
                new GroupId("b"),
                new GroupId("c"),
                new GroupId("d"),
                new GroupId("e"),
                new GroupId("f")
        ), organizations);
    }

    @Test
    @DisplayName("Cant make a Organization with a null value")
    public void cantMakeNullValuedModuleName() {
        assertThrows(
                NullPointerException.class,
                () -> new GroupId(null),
                "Should not be able to make a Organization with a null value."
        );
    }


    @Test
    @DisplayName("Can use map to update the value in a Organization")
    public void mapType() {
        assertEquals(
                new GroupId("ABC"),
                new GroupId("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapType() {
        assertThrows(
                NullPointerException.class,
                () -> new GroupId("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

}
