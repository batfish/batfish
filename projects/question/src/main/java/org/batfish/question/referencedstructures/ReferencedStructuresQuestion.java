package org.batfish.question.referencedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/** A question that provides vendor-specific information about structures referenced in files. */
public class ReferencedStructuresQuestion extends Question {
  private static final String PROP_NAMES = "names";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_TYPES = "types";

  private final @Nonnull String _names;
  private final @Nonnull NodesSpecifier _nodes;
  private final @Nonnull String _types;

  @JsonCreator
  private static ReferencedStructuresQuestion jsonCreator(
      @JsonProperty(PROP_NAMES) @Nullable String names,
      @JsonProperty(PROP_NODES) @Nullable NodesSpecifier nodes,
      @JsonProperty(PROP_TYPES) @Nullable String types) {
    String actualNames = Strings.isNullOrEmpty(names) ? ".*" : names;
    NodesSpecifier actualNodes = firstNonNull(nodes, NodesSpecifier.ALL);
    String actualTypes = Strings.isNullOrEmpty(types) ? ".*" : types;
    return new ReferencedStructuresQuestion(actualNames, actualNodes, actualTypes);
  }

  public ReferencedStructuresQuestion(
      @Nonnull String names, @Nonnull NodesSpecifier nodes, @Nonnull String types) {
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
    return "referencedStructures";
  }

  @JsonProperty(PROP_NAMES)
  public @Nonnull String getNames() {
    return _names;
  }

  @JsonProperty(PROP_NODES)
  public @Nonnull NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_TYPES)
  public @Nonnull String getTypes() {
    return _types;
  }
}
