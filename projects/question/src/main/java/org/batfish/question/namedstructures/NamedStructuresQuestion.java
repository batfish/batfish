package org.batfish.question.namedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns named structures of nodes in a tabular format. {@link
 * NamedStructuresQuestion#_nodes} determines which nodes are included, and {@link
 * NamedStructuresQuestion#_structureTypes} determines which named structures are included.
 */
@ParametersAreNonnullByDefault
public class NamedStructuresQuestion extends Question {

  private static boolean DEFAULT_IGNORE_GENERATED = true;
  private static boolean DEFAULT_INDICATE_PRESENCE = false;

  private static final String PROP_IGNORE_GENERATED = "ignoreGenerated";
  private static final String PROP_INDICATE_PRESENCE = "indicatePresence";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_STRUCTURE_NAMES = "structureNames";
  private static final String PROP_STRUCTURE_TYPES = "structureTypes";

  private final boolean _ignoreGenerated;

  private final boolean _indicatePresence;

  @Nonnull private final NodesSpecifier _nodes;

  @Nullable private final String _structureNameRegex;

  @Nonnull private final Pattern _structureNamePattern;

  @Nonnull private final NamedStructureSpecifier _structureTypes;

  @JsonCreator
  private static NamedStructuresQuestion create(
      @JsonProperty(PROP_NODES) @Nullable NodesSpecifier nodes,
      @JsonProperty(PROP_STRUCTURE_TYPES) @Nullable NamedStructureSpecifier structureTypes,
      @JsonProperty(PROP_STRUCTURE_NAMES) @Nullable String structureNameRegex,
      @JsonProperty(PROP_IGNORE_GENERATED) @Nullable Boolean ignoreGenerated,
      @JsonProperty(PROP_INDICATE_PRESENCE) @Nullable Boolean indicatePresence) {
    return new NamedStructuresQuestion(
        nodes, structureTypes, structureNameRegex, ignoreGenerated, indicatePresence);
  }

  public NamedStructuresQuestion() {
    this(null, null, null, null, null);
  }

  public NamedStructuresQuestion(NodesSpecifier nodes, NamedStructureSpecifier structureTypes) {
    this(nodes, structureTypes, null, null, null);
  }

  public NamedStructuresQuestion(
      @Nullable NodesSpecifier nodes,
      @Nullable NamedStructureSpecifier structureTypes,
      @Nullable String structureNameRegex,
      @Nullable Boolean ignoreGenerated,
      @Nullable Boolean indicatePresence) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
    _structureTypes = firstNonNull(structureTypes, NamedStructureSpecifier.ALL);
    _structureNameRegex = structureNameRegex;
    _structureNamePattern =
        Pattern.compile(firstNonNull(structureNameRegex, ".*"), Pattern.CASE_INSENSITIVE);
    _ignoreGenerated = firstNonNull(ignoreGenerated, DEFAULT_IGNORE_GENERATED);
    _indicatePresence = firstNonNull(indicatePresence, DEFAULT_INDICATE_PRESENCE);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_IGNORE_GENERATED)
  public boolean getIgnoreGenerated() {
    return _ignoreGenerated;
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
  @JsonProperty(PROP_STRUCTURE_NAMES)
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
