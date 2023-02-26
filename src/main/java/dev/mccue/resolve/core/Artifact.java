package dev.mccue.resolve.core;

public record Artifact(String url, boolean changing, boolean optional, Credentials credentials) { }
