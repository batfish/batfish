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

  @Nonnull private final String _names;
  @Nonnull private final NodesSpecifier _nodes;
  @Nonnull private final String _types;

  @JsonCreator
  private static ReferencedStructuresQuestion jsonCreator(
      @Nullable @JsonProperty(PROP_NAMES) String names,
      @Nullable @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @Nullable @JsonProperty(PROP_TYPES) String types) {
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
  @Nonnull
  public String getNames() {
    return _names;
  }

  @JsonProperty(PROP_NODES)
  @Nonnull
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_TYPES)
  @Nonnull
  public String getTypes() {
    return _types;
  }
}
