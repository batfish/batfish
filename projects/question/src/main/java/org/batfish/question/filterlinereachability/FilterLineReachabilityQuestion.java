package org.batfish.question.filterlinereachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllFiltersFilterSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns unreachable lines of ACLs in a tabular format. {@link
 * FilterLineReachabilityQuestion#_filters} determines which ACLs are checked, and {@link
 * FilterLineReachabilityQuestion#_nodes} determines which nodes are checked for those ACLs.
 */
@ParametersAreNonnullByDefault
public class FilterLineReachabilityQuestion extends Question {
  private static final boolean DEFAULT_IGNORE_COMPOSITES = true;
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_IGNORE_COMPOSITES = "ignoreComposites";
  private static final String PROP_NODES = "nodes";

  private final @Nullable String _filters;

  private final @Nonnull FilterSpecifier _filterSpecifier;

  private final boolean _ignoreComposites;

  private @Nullable String _nodes;

  private final @Nonnull NodeSpecifier _nodeSpecifier;

  @JsonCreator
  private static FilterLineReachabilityQuestion create(
      @JsonProperty(PROP_FILTERS) @Nullable String filters,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_IGNORE_COMPOSITES) @Nullable Boolean ignoreComposites) {
    return new FilterLineReachabilityQuestion(
        filters, nodes, firstNonNull(ignoreComposites, DEFAULT_IGNORE_COMPOSITES));
  }

  @VisibleForTesting
  FilterLineReachabilityQuestion() {
    this((String) null, null, DEFAULT_IGNORE_COMPOSITES);
  }

  @VisibleForTesting
  FilterLineReachabilityQuestion(String filters) {
    this(filters, null, DEFAULT_IGNORE_COMPOSITES);
  }

  public FilterLineReachabilityQuestion(
      @Nullable String filters, @Nullable String nodes, boolean ignoreComposites) {
    this(
        filters,
        SpecifierFactories.getFilterSpecifierOrDefault(filters, AllFiltersFilterSpecifier.INSTANCE),
        nodes,
        SpecifierFactories.getNodeSpecifierOrDefault(nodes, AllNodesNodeSpecifier.INSTANCE),
        ignoreComposites);
  }

  public FilterLineReachabilityQuestion(
      FilterSpecifier filterSpecifier, NodeSpecifier nodeSpecifier, boolean ignoreComposites) {
    this(null, filterSpecifier, null, nodeSpecifier, ignoreComposites);
  }

  private FilterLineReachabilityQuestion(
      @Nullable String filters,
      FilterSpecifier filterSpecifier,
      @Nullable String nodes,
      NodeSpecifier nodeSpecifier,
      boolean ignoreComposites) {
    _filters = filters;
    _filterSpecifier = filterSpecifier;
    _nodes = nodes;
    _nodeSpecifier = nodeSpecifier;
    _ignoreComposites = ignoreComposites;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTERS)
  public @Nullable String getFilters() {
    return _filters;
  }

  @JsonIgnore
  @Nonnull
  FilterSpecifier getFilterSpecifier() {
    return _filterSpecifier;
  }

  @JsonProperty(PROP_IGNORE_COMPOSITES)
  public boolean getIgnoreComposites() {
    return _ignoreComposites;
  }

  @Override
  public String getName() {
    return "filterLineReachability";
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  @Nonnull
  NodeSpecifier nodeSpecifier() {
    return _nodeSpecifier;
  }
}
