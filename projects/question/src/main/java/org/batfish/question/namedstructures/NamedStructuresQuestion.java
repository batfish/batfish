package org.batfish.question.namedstructures;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns named structures of nodes in a tabular format. {@link
 * NamedStructuresQuestion#_nodes} determines which nodes are included, and {@link
 * NamedStructuresQuestion#_structureTypeSpecifier} determines which named structures are included.
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

  @Nullable private final String _nodes;

  @Nonnull private final NodeSpecifier _nodeSpecifier;

  @Nullable private final String _structureNameRegex;

  @Nonnull private final Pattern _structureNamePattern;

  @Nullable private final String _structureTypes;

  @Nonnull private final NamedStructurePropertySpecifier _structureTypeSpecifier;

  @JsonCreator
  static NamedStructuresQuestion create(
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_STRUCTURE_TYPES) @Nullable String structureTypes,
      @JsonProperty(PROP_STRUCTURE_NAMES) @Nullable String structureNameRegex,
      @JsonProperty(PROP_IGNORE_GENERATED) @Nullable Boolean ignoreGenerated,
      @JsonProperty(PROP_INDICATE_PRESENCE) @Nullable Boolean indicatePresence) {
    return new NamedStructuresQuestion(
        nodes, structureTypes, structureNameRegex, ignoreGenerated, indicatePresence);
  }

  NamedStructuresQuestion(
      @Nullable String nodes,
      @Nullable String structureTypes,
      @Nullable String structureNameRegex,
      @Nullable Boolean ignoreGenerated,
      @Nullable Boolean indicatePresence) {
    this(
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        structureTypes,
        NamedStructurePropertySpecifier.create(structureTypes),
        structureNameRegex,
        ignoreGenerated,
        indicatePresence);
  }

  public NamedStructuresQuestion(
      @Nonnull NodeSpecifier nodeSpecifier,
      @Nonnull NamedStructurePropertySpecifier structureTypeSpecifier,
      @Nullable String structureNameRegex,
      @Nullable Boolean ignoreGenerated,
      @Nullable Boolean indicatePresence) {
    this(
        null,
        nodeSpecifier,
        null,
        structureTypeSpecifier,
        structureNameRegex,
        ignoreGenerated,
        indicatePresence);
  }

  private NamedStructuresQuestion(
      @Nullable String nodes,
      @Nonnull NodeSpecifier nodeSpecifier,
      @Nullable String structureTypes,
      @Nonnull NamedStructurePropertySpecifier structureTypeSpecifier,
      @Nullable String structureNameRegex,
      @Nullable Boolean ignoreGenerated,
      @Nullable Boolean indicatePresence) {
    _nodes = nodes;
    _nodeSpecifier = nodeSpecifier;
    _structureTypes = structureTypes;
    _structureTypeSpecifier = structureTypeSpecifier;
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

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nonnull
  @JsonIgnore
  public NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
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

  @Nullable
  @JsonProperty(PROP_STRUCTURE_TYPES)
  public String getStructureTypes() {
    return _structureTypes;
  }

  @Nonnull
  @JsonIgnore
  public NamedStructurePropertySpecifier getStructureTypeSpecifier() {
    return _structureTypeSpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NamedStructuresQuestion)) {
      return false;
    }
    NamedStructuresQuestion that = (NamedStructuresQuestion) o;
    return _ignoreGenerated == that._ignoreGenerated
        && _indicatePresence == that._indicatePresence
        && Objects.equals(_nodes, that._nodes)
        && _nodeSpecifier.equals(that._nodeSpecifier)
        && Objects.equals(_structureNameRegex, that._structureNameRegex)
        && _structureNamePattern.toString().equals(that._structureNamePattern.toString())
        && Objects.equals(_structureTypes, that._structureTypes)
        && _structureTypeSpecifier.equals(that._structureTypeSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _ignoreGenerated,
        _indicatePresence,
        _nodes,
        _nodeSpecifier,
        _structureNameRegex,
        _structureNamePattern,
        _structureTypes,
        _structureTypeSpecifier);
  }
}
