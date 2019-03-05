package org.batfish.question.comparefilters;

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
  private static final String PROP_NODES = "nodes";

  private static final FilterSpecifier DEFAULT_FILTER_SPECIFIER =
      AllFiltersFilterSpecifier.INSTANCE;
  private static final NodeSpecifier DEFAULT_NODE_SPECIFIER = AllNodesNodeSpecifier.INSTANCE;

  @Nullable private final String _filters;
  @Nullable private final String _nodes;

  CompareFiltersQuestion() {
    this(null, null);
  }

  CompareFiltersQuestion(@Nullable String filters, @Nullable String nodes) {
    _filters = filters;
    _nodes = nodes;
    setDifferential(true);
  }

  @JsonCreator
  private static CompareFiltersQuestion jsonCreator(
      @JsonProperty(PROP_FILTERS) @Nullable String filters,
      @JsonProperty(PROP_NODES) @Nullable String nodes) {
    return new CompareFiltersQuestion(filters, nodes);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "compareFilters";
  }

  @Nullable
  @JsonProperty(PROP_FILTERS)
  public String getFilters() {
    return _filters;
  }

  @Nonnull
  @JsonIgnore
  FilterSpecifier getFilterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(_filters, DEFAULT_FILTER_SPECIFIER);
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nonnull
  @JsonIgnore
  NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, DEFAULT_NODE_SPECIFIER);
  }
}
