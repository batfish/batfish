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

  private static boolean DEFAULT_INDICATE_PRESENCE = false;

  private static final String PROP_INDICATE_PRESENCE = "indicatePresence";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_STRUCTURE_TYPES = "structureTypes";

  private final boolean _indicatePresence;

  private final NodesSpecifier _nodes;

  @Nonnull private final NamedStructureSpecifier _structureTypes;

  public NamedStructuresQuestion(
      @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @JsonProperty(PROP_STRUCTURE_TYPES) NamedStructureSpecifier structureTypes,
      @JsonProperty(PROP_INDICATE_PRESENCE) Boolean indicatePresence) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
    _structureTypes = firstNonNull(structureTypes, NamedStructureSpecifier.ALL);
    _indicatePresence = firstNonNull(indicatePresence, DEFAULT_INDICATE_PRESENCE);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_INDICATE_PRESENCE)
  public boolean getIndicatePresence() {
    return _indicatePresence;
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
