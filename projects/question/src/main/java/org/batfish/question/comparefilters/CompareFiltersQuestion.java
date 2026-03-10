package org.batfish.question.comparefilters;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllFiltersFilterSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** A question to compare the flows permitted/denied by two filters. */
@ParametersAreNonnullByDefault
public final class CompareFiltersQuestion extends Question {
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_IGNORE_COMPOSITES = "ignoreComposites";
  private static final String PROP_NODES = "nodes";

  private static final FilterSpecifier DEFAULT_FILTER_SPECIFIER =
      AllFiltersFilterSpecifier.INSTANCE;
  private static final Boolean DEFAULT_IGNORE_COMPOSITES = Boolean.TRUE;
  private static final NodeSpecifier DEFAULT_NODE_SPECIFIER = AllNodesNodeSpecifier.INSTANCE;

  private final @Nullable String _filters;
  private final boolean _ignoreComposites;
  private final @Nullable String _nodes;

  CompareFiltersQuestion() {
    this(null, null, null);
  }

  CompareFiltersQuestion(
      @Nullable String filters, @Nullable Boolean ignoreComposites, @Nullable String nodes) {
    _filters = filters;
    _ignoreComposites = firstNonNull(ignoreComposites, DEFAULT_IGNORE_COMPOSITES);
    _nodes = nodes;
    setDifferential(true);
  }

  @JsonCreator
  private static CompareFiltersQuestion jsonCreator(
      @JsonProperty(PROP_FILTERS) @Nullable String filters,
      @JsonProperty(PROP_IGNORE_COMPOSITES) @Nullable Boolean ignoreComposites,
      @JsonProperty(PROP_NODES) @Nullable String nodes) {
    return new CompareFiltersQuestion(filters, ignoreComposites, nodes);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "compareFilters";
  }

  @JsonProperty(PROP_FILTERS)
  public @Nullable String getFilters() {
    return _filters;
  }

  @JsonIgnore
  @Nonnull
  FilterSpecifier getFilterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(_filters, DEFAULT_FILTER_SPECIFIER);
  }

  @JsonProperty(PROP_IGNORE_COMPOSITES)
  public boolean getIgnoreComposites() {
    return _ignoreComposites;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, DEFAULT_NODE_SPECIFIER);
  }
}
