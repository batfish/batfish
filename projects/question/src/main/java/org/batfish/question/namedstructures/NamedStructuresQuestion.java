package org.batfish.question.namedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  private static final String PROP_STRUCTURE_NAME = "structureNames";
  private static final String PROP_STRUCTURE_TYPES = "structureTypes";

  private final boolean _indicatePresence;

  @Nonnull private final NodesSpecifier _nodes;

  @Nullable private final String _structureNameRegex;

  @Nonnull private final Pattern _structureNamePattern;

  @Nonnull private final NamedStructureSpecifier _structureTypes;

  @JsonCreator
  private static NamedStructuresQuestion create(
      @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @JsonProperty(PROP_STRUCTURE_TYPES) NamedStructureSpecifier structureTypes,
      @JsonProperty(PROP_INDICATE_PRESENCE) Boolean indicatePresence,
      @JsonProperty(PROP_STRUCTURE_NAME) String structureNameRegex) {
    return new NamedStructuresQuestion(
        firstNonNull(nodes, NodesSpecifier.ALL),
        firstNonNull(structureTypes, NamedStructureSpecifier.ALL),
        firstNonNull(indicatePresence, DEFAULT_INDICATE_PRESENCE),
        structureNameRegex);
  }

  public NamedStructuresQuestion() {
    this(NodesSpecifier.ALL, NamedStructureSpecifier.ALL, DEFAULT_INDICATE_PRESENCE, null);
  }

  public NamedStructuresQuestion(NodesSpecifier nodes, NamedStructureSpecifier structureTypes) {
    this(nodes, structureTypes, DEFAULT_INDICATE_PRESENCE, null);
  }

  public NamedStructuresQuestion(
      NodesSpecifier nodes, NamedStructureSpecifier structureTypes, boolean indicatePresence) {
    this(nodes, structureTypes, indicatePresence, null);
  }

  public NamedStructuresQuestion(
      NodesSpecifier nodes,
      NamedStructureSpecifier structureTypes,
      boolean indicatePresence,
      @Nullable String structureNameRegex) {
    _nodes = nodes;
    _structureTypes = structureTypes;
    _indicatePresence = indicatePresence;
    _structureNameRegex = structureNameRegex;
    _structureNamePattern =
        Pattern.compile(firstNonNull(structureNameRegex, ".*"), Pattern.CASE_INSENSITIVE);
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

  @Nonnull
  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }

  @Nullable
  @JsonProperty(PROP_STRUCTURE_NAME)
  public String getStructureNameRegex() {
    return _structureNameRegex;
  }

  @Nonnull
  @JsonIgnore
  public Pattern getStructureNamePattern() {
    return _structureNamePattern;
  }

  @Nonnull
  @JsonProperty(PROP_STRUCTURE_TYPES)
  public NamedStructureSpecifier getStructureTypes() {
    return _structureTypes;
  }
}
