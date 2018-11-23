package org.batfish.question.namedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns named structures of nodes in a tabular format. {@link
 * NamedStructuresQuestion#_nodes} determines which nodes are included, and {@link
 * NamedStructuresQuestion#_structureTypes} determines which named structures are included.
 */
public class NamedStructuresQuestion extends Question {

  private static final String PROP_NODES = "nodes";
  private static final String PROP_STRUCTURE_TYPES = "structureTypes";

  private final NodesSpecifier _nodes;

  @Nonnull private NamedStructureSpecifier _structureTypes;

  public NamedStructuresQuestion(
      @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @JsonProperty(PROP_STRUCTURE_TYPES) NamedStructureSpecifier structureTypes) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
    _structureTypes = firstNonNull(structureTypes, NamedStructureSpecifier.ALL);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "namedStructures";
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_STRUCTURE_TYPES)
  public NamedStructureSpecifier getStructureTypes() {
    return _structureTypes;
  }
}
