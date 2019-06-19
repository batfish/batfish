package org.batfish.question.definedstructures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Fetches defined structures in config files. */
public class DefinedStructuresQuestion extends Question {
  private static final String PROP_NAMES = "names";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_TYPES = "types";

  @Nonnull private final String _names;
  @Nullable private final String _nodes;
  @Nonnull private final String _types;

  @JsonCreator
  private static DefinedStructuresQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_NAMES) String names,
      @Nullable @JsonProperty(PROP_NODES) String nodes,
      @Nullable @JsonProperty(PROP_TYPES) String types) {
    String actualNames = Strings.isNullOrEmpty(names) ? ".*" : names;
    String actualTypes = Strings.isNullOrEmpty(types) ? ".*" : types;
    return new DefinedStructuresQuestion(actualNames, nodes, actualTypes);
  }

  public DefinedStructuresQuestion(
      @Nonnull String names, @Nullable String nodes, @Nonnull String types) {
    _names = names;
    _nodes = nodes;
    _types = types;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "definedStructures";
  }

  @JsonProperty(PROP_NAMES)
  @Nonnull
  public String getNames() {
    return _names;
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nonnull
  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_TYPES)
  @Nonnull
  public String getTypes() {
    return _types;
  }
}
