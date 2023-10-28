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
  /** An optional filename for all defined structures returned. */
  private static final String PROP_FILENAME = "filename";

  /** A filter on nodes for which to return files defining structures. */
  private static final String PROP_NODES = "nodes";

  /** A filter on structure names. */
  private static final String PROP_NAMES = "names";

  /** A filter on vendor-specific structure types. */
  private static final String PROP_TYPES = "types";

  private final @Nullable String _filename;
  private final @Nullable String _nodes;
  private final @Nonnull String _types;
  private final @Nonnull String _names;

  @JsonCreator
  private static DefinedStructuresQuestion jsonCreator(
      @JsonProperty(PROP_FILENAME) @Nullable String filename,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_TYPES) @Nullable String types,
      @JsonProperty(PROP_NAMES) @Nullable String names) {
    String actualFilename = Strings.isNullOrEmpty(filename) ? null : filename;
    String actualNodes = Strings.isNullOrEmpty(nodes) ? null : nodes;
    String actualTypes = Strings.isNullOrEmpty(types) ? ".*" : types;
    String actualNames = Strings.isNullOrEmpty(names) ? ".*" : names;
    return new DefinedStructuresQuestion(actualFilename, actualNodes, actualTypes, actualNames);
  }

  public DefinedStructuresQuestion(
      @Nullable String filename,
      @Nullable String nodes,
      @Nonnull String types,
      @Nonnull String names) {
    _filename = filename;
    _nodes = nodes;
    _types = types;
    _names = names;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "definedStructures";
  }

  @JsonProperty(PROP_FILENAME)
  public @Nullable String getFilename() {
    return _filename;
  }

  @JsonProperty(PROP_NAMES)
  public @Nonnull String getNames() {
    return _names;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_TYPES)
  public @Nonnull String getTypes() {
    return _types;
  }
}
