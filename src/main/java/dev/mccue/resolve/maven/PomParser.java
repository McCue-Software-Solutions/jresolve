package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.*;
import dev.mccue.resolve.core.Library;
import dev.mccue.resolve.util.LL;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

public final class PomParser extends DefaultHandler {
    final State state = new State();
    LL<String> paths = new LL.Nil<>();

    // Each "path" in the document has potentially a registered handler.
    // If it doesn't have one, we still want to track it so popping and
    LL<Optional<Handler>> handlers = new LL.Nil<>();

    // handlers
    StringBuilder characterBuffer = new StringBuilder();

    // dependencies, dependency, artifactId
    @Override
    public void startElement(String _uri, String _localName, String tagName, org.xml.sax.Attributes _attributes) {
        var paths = this.paths.prepend(tagName);
        this.paths = paths;
        var handler = HANDLER_MAP.getOrDefault(
                this.paths,
                HANDLER_MAP.get(paths.tail().prepend("*"))
        );

        this.handlers = this.handlers.prepend(Optional.ofNullable(handler));

        if (handler != null) {
            switch (handler) {
                case ContentHandler __ -> {
                }
                case SectionHandler s -> s.start(state);
                case PropertyHandler p -> p.name(state, tagName);
            }
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) {
        boolean readContent = this.handlers.headOption()
                .flatMap(handlerOpt -> handlerOpt.map(handler ->
                        switch (handler) {
                            case PropertyHandler __ -> true;
                            case ContentHandler __ -> true;
                            case SectionHandler __ -> false;
                        }
                ))
                .orElse(false);

        if (readContent) {
            characterBuffer.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String _uri, String _localName, String tagName) {
        var handlerOpt = handlers.headOption().flatMap(Function.identity());
        // Calling endElement implies startElement was called,
        // which means a non-empty paths and handlers
        this.paths = this.paths.assumeNotEmpty().tail();
        this.handlers = this.handlers.assumeNotEmpty().tail();

        handlerOpt.ifPresent(handler -> {
            switch (handler) {
                case PropertyHandler p -> p.content(state, characterBuffer.toString());
                case ContentHandler c -> c.content(state, characterBuffer.toString());
                case SectionHandler s -> s.end(state);
            }
        });

        characterBuffer.setLength(0);
    }

    private final class State {
        String groupId = "";
        Optional<String> artifactIdOpt = Optional.empty();
        String version = "";
        Optional<String> parentGroupIdOpt = Optional.empty();
        Optional<String> parentArtifactIdOpt = Optional.empty();
        String parentVersion = "";

        String description = "";
        String url = "";


        Optional<Type> packagingOpt = Optional.empty();
        final ArrayList<Tuple2<Configuration, Dependency>> dependencies = new ArrayList<>();
        final ArrayList<Tuple2<Configuration, Dependency>> dependencyManagement = new ArrayList<>();

        final HashMap<String, String> properties = new HashMap<>();

        Optional<GroupId> relocationGroupIdOpt = Optional.empty();
        Optional<ArtifactId> relocationArtifactIdOpt = Optional.empty();
        Optional<String> relocationVersionOpt = Optional.empty();

        Optional<GroupId> dependencyGroupIdOpt = Optional.empty();
        Optional<ArtifactId> dependencyArtifactIdOpt = Optional.empty();
        String dependencyVersion = "";
        boolean dependencyOptional = false;
        Configuration dependencyScope = Configuration.EMPTY;
        Type dependencyType = Type.EMPTY;
        Classifier dependencyClassifier = Classifier.EMPTY;
        final HashSet<Exclusion> dependencyExclusions = new HashSet<>();

        GroupId dependencyExclusionGroupId = GroupId.ALL;
        ArtifactId dependencyExclusionArtifactId = ArtifactId.ALL;

        Optional<String> propertyNameOpt = Optional.empty();

        String profileId = "";
        final ArrayList<Tuple2<Configuration, Dependency>> profileDependencies = new ArrayList<>();
        final ArrayList<Tuple2<Configuration, Dependency>> profileDependencyManagement = new ArrayList<>();
        final HashMap<String, String> profileProperties = new HashMap<>();
        final ArrayList<Tuple2<String, Optional<String>>> profileActivationProperties = new ArrayList<>();
        Optional<Boolean> profileActiveByDefaultOpt = Optional.empty();
        Optional<String> profilePropertyNameOpt = Optional.empty();
        Optional<String> profilePropertyValueOpt = Optional.empty();

        Optional<String> profileActivationOsArchOpt = Optional.empty();
        Optional<String> profileActivationOsFamilyOpt = Optional.empty();
        Optional<String> profileActivationOsNameOpt = Optional.empty();
        Optional<String> profileActivationOsVersionOpt = Optional.empty();
        Activation.Jdk profileActivationJdkOpt = new Activation.Jdk.Unspecified();

        final ArrayList<Profile> profiles = new ArrayList<>();

        Project project() throws ModelParseException{
            final Optional<String> groupIdOpt;
            if (!groupId.isEmpty()) {
                groupIdOpt = Optional.of(groupId);
            } else {
                groupIdOpt = parentGroupIdOpt.filter(s -> !s.isEmpty());
            }

            final Optional<String> versionOpt;
            if (!version.isEmpty()) {
                versionOpt = Optional.of(version);
            } else {
                versionOpt = Optional.of(parentVersion).filter(s -> !s.isEmpty());
            }

            var dependencies0 = new ArrayList<Tuple2<Configuration, Dependency>>();

            for (var dependency : dependencies) {
                final var matcher = Pattern.compile("\\$\\{(.*?)\\}").matcher(dependency.second().version());
                if (matcher.find()) {
                        final var variable = matcher.group(1);
                        if (state.properties.containsKey(variable)) {
                               dependencies0.add(new Tuple2<Configuration, Dependency>(dependency.first(), dependency.second().withVersion(matcher.replaceAll(state.properties.get(variable))))); 
                        } else {
                                throw new ModelParseException("Undefined variable " + variable + " used in the POM");
                        }
                } else {
                        dependencies0.add(dependency);
                }
            }

            var properties0 = Map.copyOf(properties);

            final Optional<Library> parentModuleOpt;
            {
                var parentGroupId = parentGroupIdOpt.orElse(null);
                var parentArtifactId = parentArtifactIdOpt.orElse(null);
                if (parentGroupId != null && parentArtifactId != null) {
                    parentModuleOpt = Optional.of(new Library(
                            new GroupId(parentGroupId),
                            new ArtifactId(parentArtifactId)
                    ));
                } else {
                    parentModuleOpt = Optional.empty();
                }
            }

            var finalGroupId = groupIdOpt.orElseThrow(() -> new RuntimeException("No organization found"));
            var artifactId = artifactIdOpt.orElseThrow(() -> new RuntimeException("No artifactId found"));
            var finalVersion = versionOpt.orElseThrow(() -> new RuntimeException("No version found"));

            var parentModule = parentModuleOpt.orElse(null);
            if (parentModule != null && parentModule.groupId().value().isEmpty()) {
                throw new RuntimeException("Parent organization missing");
            }

            if (parentModule != null && parentVersion.isEmpty()) {
                throw new RuntimeException("No parent version found");
            }


            // TODO
            for (var entry : properties0.entrySet()) {
                if ("extraDependencyAttributes".equals(entry.getKey())) {
                    var s = entry.getValue();
                }
            }

            var parentOpt = parentModuleOpt.map(module -> new Tuple2<>(
                    module, parentVersion
            ));

            var extraAttrs = properties0
                    .entrySet()
                    .stream()
                    .filter(pair -> pair.getKey().equals("extraDependencyAttributes"))
                    .findFirst();//.orElse(Map.of());

            var projModule = new Library(
                    new GroupId(finalGroupId),
                    new ArtifactId(artifactId)
            );


            // TODO: relocationDependencyOpt, etc

            return new Project(
                    projModule,
                    finalVersion,
                    dependencies0, // TODO
                    Map.of(),
                    parentOpt,
                    List.copyOf(dependencyManagement),
                    properties0,
                    List.copyOf(profiles),
                    Optional.empty(),
                    Optional.empty(),
                    packagingOpt,
                    false, // TODO
                    Optional.empty(),
                    List.of()
            );
        }
    }

    private sealed interface Handler {
        LL<String> path();
    }

    private non-sealed interface SectionHandler extends Handler {
        void start(State state);

        void end(State state);
    }

    private non-sealed interface ContentHandler extends Handler {
        void content(State state, String content);
    }

    private non-sealed interface PropertyHandler extends Handler {
        void name(State state, String name);

        void content(State state, String content);
    }

    private interface AddDepHandler {
        void add(State state, Configuration configuration, Dependency dependency);
    }

    private static List<Handler> dependencyHandlers (
            LL<String> prefix,
            AddDepHandler addDepHandler
    ) {
        return List.of(
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.dependencyGroupIdOpt = Optional.empty();
                        state.dependencyArtifactIdOpt = Optional.empty();
                        state.dependencyVersion = "";
                        state.dependencyOptional = false;
                        state.dependencyScope = Configuration.EMPTY;
                        state.dependencyType = Type.EMPTY;
                        state.dependencyClassifier = Classifier.EMPTY;
                        state.dependencyExclusions.clear();
                    }

                    @Override
                    public void end(State state) {
                        var dependency = new Dependency(
                                new Library(
                                        state.dependencyGroupIdOpt.orElseThrow(() -> new RuntimeException(
                                                "Expected a groupId on dependency"
                                        )),
                                        state.dependencyArtifactIdOpt.orElseThrow(() -> new RuntimeException(
                                                "Expected an artifactId on dependency"
                                        ))
                                ),
                                state.dependencyVersion,
                                Configuration.EMPTY,
                                state.dependencyExclusions,
                                new Attributes(state.dependencyType, state.dependencyClassifier),
                                state.dependencyOptional,
                                true
                        );
                        addDepHandler.add(
                                state,
                                state.dependencyScope,
                                dependency
                        );
                    }

                    @Override
                    public LL<String> path() {
                        return prefix;
                    }
                },
                content(
                        new LL.Cons<>("groupId", prefix),
                        (state, content) ->
                                state.dependencyGroupIdOpt =
                                        Optional.of(new GroupId(content))
                ),
                content(
                        new LL.Cons<>("artifactId", prefix),
                        (state, content) ->
                                state.dependencyArtifactIdOpt =
                                        Optional.of(new ArtifactId(content))
                ),
                content(
                        new LL.Cons<>("version", prefix),
                        (state, content) -> 
                                state.dependencyVersion = content
                ),
                content(
                        new LL.Cons<>("optional", prefix),
                        (state, content) ->
                                state.dependencyOptional = "true".equals(content)
                ),
                content(
                        new LL.Cons<>("scope", prefix),
                        (state, content) ->
                                state.dependencyScope = new Configuration(content)
                ),
                content(
                        new LL.Cons<>("type", prefix),
                        (state, content) ->
                                state.dependencyType = new Type(content)
                ),
                content(
                        new LL.Cons<>("classifier", prefix),
                        (state, content) ->
                                state.dependencyClassifier = new Classifier(content)
                ),
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.dependencyExclusionGroupId = GroupId.ALL;
                        state.dependencyExclusionArtifactId = ArtifactId.ALL;
                    }

                    @Override
                    public void end(State state) {
                        state.dependencyExclusions.add(
                                new Exclusion(
                                        state.dependencyExclusionGroupId,
                                        state.dependencyExclusionArtifactId
                                )
                        );
                    }

                    @Override
                    public LL<String> path() {
                        return new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix));
                    }
                },
                content(
                        new LL.Cons<>("groupId", new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix))),
                        (state, content) ->
                                state.dependencyExclusionGroupId = new GroupId(content)
                ),
                content(
                        new LL.Cons<>("artifactId", new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix))),
                        (state, content) ->
                                state.dependencyExclusionArtifactId = new ArtifactId(content)
                )
        );
    }

    private interface AddPropertyHandler {
        void add(State state, String key, String value);
    }

    private static List<Handler> propertyHandlers(
            LL<String> prefix,
            AddPropertyHandler add
    ) {
        return List.of(
                new PropertyHandler() {
                    @Override
                    public void name(State state, String name) {
                        state.propertyNameOpt = Optional.of(name);
                    }

                    @Override
                    public void content(State state, String content) {
                        add.add(
                                state,
                                state.propertyNameOpt.orElseThrow(() ->
                                        new IllegalStateException("Property artifactId should have been set by the artifactId method")
                                ),
                                content
                        );
                        state.propertyNameOpt = Optional.empty();
                    }

                    @Override
                    public LL<String> path() {
                        return new LL.Cons<>("*", prefix);
                    }
                }
        );
    }

    private interface AddProfileHandler {
        void add(State state, Profile profile);
    }

    private static List<Handler> profileHandlers(
            LL<String> prefix,
            AddProfileHandler add
    ) {
        var handlers = new ArrayList<>(List.of(
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.profileId = "";
                        state.profileActiveByDefaultOpt = Optional.empty();
                        state.profileDependencies.clear();
                        state.profileDependencyManagement.clear();
                        state.profileProperties.clear();
                        state.profileActivationProperties.clear();
                        state.profileActivationOsArchOpt = Optional.empty();
                        state.profileActivationOsFamilyOpt = Optional.empty();
                        state.profileActivationOsNameOpt = Optional.empty();
                        state.profileActivationOsVersionOpt = Optional.empty();
                        state.profileActivationJdkOpt = new Activation.Jdk.Unspecified();
                    }

                    @Override
                    public void end(State state) {
                        var profile = new Profile(
                                state.profileId,
                                state.profileActiveByDefaultOpt,
                                new Activation(
                                        List.copyOf(state.profileActivationProperties),
                                        new Activation.Os(
                                                state.profileActivationOsArchOpt,
                                                state.profileActivationOsFamilyOpt.stream()
                                                        .collect(Collectors.toUnmodifiableSet()),
                                                state.profileActivationOsNameOpt,
                                                state.profileActivationOsVersionOpt
                                        ),
                                        state.profileActivationJdkOpt
                                ),
                                List.copyOf(state.profileDependencies),
                                List.copyOf(state.profileDependencyManagement),
                                Map.copyOf(state.profileProperties)
                        );

                        add.add(state, profile);
                    }

                    @Override
                    public LL<String> path() {
                        return prefix;
                    }
                },
                content(
                        new LL.Cons<>("id", prefix),
                        (state, content) ->
                                state.profileId = content
                ),
                content(
                        new LL.Cons<>("activeByDefault", new LL.Cons<>("activation", new LL.Nil<>())),
                        (state, content) ->
                                state.profileActiveByDefaultOpt = switch (content) {
                                    case "true" -> Optional.of(true);
                                    case "false" -> Optional.of(false);
                                    default -> Optional.empty();
                                }
                ),

                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.profilePropertyNameOpt = Optional.empty();
                        state.profilePropertyValueOpt = Optional.empty();
                    }

                    @Override
                    public void end(State state) {
                        state.profileActivationProperties.add(
                                new Tuple2<>(
                                        state.profilePropertyNameOpt.orElseThrow(() ->
                                                new RuntimeException("Expected profile property artifactId to be set")
                                        ),
                                        state.profilePropertyValueOpt
                                )
                        );
                    }

                    @Override
                    public LL<String> path() {
                        return new LL.Cons<>("property", new LL.Cons<>("activation", prefix));
                    }
                },
                content(
                        new LL.Cons<>("artifactId", new LL.Cons<>("property", new LL.Cons<>("activation", prefix))),
                        (state, content) ->
                                state.profilePropertyNameOpt = Optional.of(content)
                ),
                content(
                        new LL.Cons<>("value", new LL.Cons<>("property", new LL.Cons<>("activation", prefix))),
                        (state, content) ->
                                state.profilePropertyValueOpt = Optional.of(content)
                ),
                content(
                        new LL.Cons<>("arch", new LL.Cons<>("os", new LL.Cons<>("activation", prefix))),
                        (state, content) ->
                                state.profileActivationOsArchOpt = Optional.of(content)
                ),
                content(
                        new LL.Cons<>("family", new LL.Cons<>("os", new LL.Cons<>("activation", prefix))),
                        (state, content) ->
                                state.profileActivationOsFamilyOpt = Optional.of(content)
                ),
                content(
                        new LL.Cons<>("artifactId", new LL.Cons<>("os", new LL.Cons<>("activation", prefix))),
                        (state, content) ->
                                state.profileActivationOsNameOpt = Optional.of(content)
                ),
                content(
                        new LL.Cons<>("version", new LL.Cons<>("os", new LL.Cons<>("activation", prefix))),
                        (state, content) ->
                                state.profileActivationOsVersionOpt = Optional.of(content)
                ),
                content(
                        new LL.Cons<>("jdk", new LL.Cons<>("activation", prefix)),
                        (state, content) -> {
                            // TODO
                        }
                )
        ));

        handlers.addAll(dependencyHandlers(
                new LL.Cons<>("dependency", new LL.Cons<>("dependencies", prefix)),
                (state, configuration, dependency) ->
                        state.profileDependencies.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(dependencyHandlers(
                new LL.Cons<>("dependency", new LL.Cons<>("dependencies", new LL.Cons<>("dependencyManagement", prefix))),
                (state, configuration, dependency) ->
                        state.profileDependencyManagement.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(propertyHandlers(
                new LL.Cons<>("properties", prefix),
                (state, key, value) ->
                        state.profileProperties.put(key, value)
        ));

        return handlers;
    }

    private static Handler content(List<String> path, BiConsumer<State, String> callback) {
        return content(LL.fromJavaList(path), callback);
    }

    private static Handler content(LL<String> path, BiConsumer<State, String> callback) {
        return new ContentHandler() {
            @Override
            public void content(State state, String content) {
                callback.accept(state, content);
            }

            @Override
            public LL<String> path() {
                return path;
            }
        };
    }

    private static final Map<LL<String>, Handler> HANDLER_MAP;

    static {
        List<Handler> handlers = new ArrayList<>();
        handlers.addAll(List.of(
                content(
                        List.of("groupId", "project"),
                        (state, content) ->
                                state.groupId = content
                ),
                content(
                        List.of("artifactId", "project"),
                        (state, content) ->
                                state.artifactIdOpt = Optional.of(content)
                ),
                content(
                        List.of("version", "project"),
                        (state, content) ->
                                state.version = content
                ),
                content(
                        List.of("groupId", "parent", "project"),
                        (state, content) ->
                                state.parentGroupIdOpt = Optional.of(content)
                ),
                content(
                        List.of("artifactId", "parent", "project"),
                        (state, content) ->
                                state.parentArtifactIdOpt = Optional.of(content)
                ),
                content(
                        List.of("version", "parent", "project"),
                        (state, content) ->
                                state.parentVersion = content
                ),
                content(
                        List.of("description", "project"),
                        (state, content) ->
                                state.description = content
                ),

                content(
                        List.of("url", "project"),
                        (state, content) ->
                                state.url = content
                ),

                content(
                        List.of("packaging", "project"),
                        (state, content) ->
                                state.packagingOpt = Optional.of(new Type(content))
                ),
                content(
                        List.of("groupId", "relocation", "distributionManagement", "project"),
                        (state, content) ->
                                state.relocationGroupIdOpt = Optional.of(
                                        new GroupId(content)
                                )
                ),
                content(
                        List.of("artifactId", "relocation", "distributionManagement", "project"),
                        (state, content) ->
                                state.relocationArtifactIdOpt = Optional.of(
                                        new ArtifactId(content)
                                )
                ),
                content(
                        List.of("version", "relocation", "distributionManagement", "project"),
                        (state, content) ->
                                state.relocationVersionOpt = Optional.of(content)
                )
        ));

        handlers.addAll(dependencyHandlers(
                LL.fromJavaList(List.of("dependency", "dependencies", "project")),
                (state, configuration, dependency) ->
                        state.dependencies.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(dependencyHandlers(
                LL.fromJavaList(List.of("dependency", "dependencies", "dependencyManagement", "project")),
                (state, configuration, dependency) ->
                        state.dependencyManagement.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(propertyHandlers(
                LL.fromJavaList(List.of("properties", "project")),
                (state, key, value) ->
                        state.properties.put(key, value)
        ));

        handlers.addAll(profileHandlers(
                LL.fromJavaList(List.of("profile", "profiles", "project")),
                (state, profile) ->
                        state.profiles.add(profile)
        ));
        HANDLER_MAP = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Handler::path,
                        handler -> handler
                ));
    }

    public Project project() throws ModelParseException {
        return this.state.project();
    }
}
