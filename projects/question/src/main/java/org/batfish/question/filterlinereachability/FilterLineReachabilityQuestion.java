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
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/**
 * A question that returns unreachable lines of ACLs in a tabular format. {@link
 * FilterLineReachabilityQuestion#_filterSpecifierInput} determines which ACLs are checked, and
 * {@link FilterLineReachabilityQuestion#_nodeSpecifierInput} determines which nodes are checked for
 * those ACLs.
 */
@ParametersAreNonnullByDefault
public class FilterLineReachabilityQuestion extends Question {
  private static final String DEFAULT_FILTER_SPECIFIER_FACTORY =
      FlexibleFilterSpecifierFactory.NAME;

  private static final boolean DEFAULT_IGNORE_COMPOSITES = true;

  private static final String DEFAULT_NODE_SPECIFIER_FACTORY = FlexibleNodeSpecifierFactory.NAME;

  private static final String PROP_FILTER_SPECIFIER_FACTORY = "filterSpecifierFactory";

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filters";

  private static final String PROP_IGNORE_COMPOSITES = "ignoreComposites";

  private static final String PROP_NODE_SPECIFIER_FACTORY = "nodeSpecifierFactory";

  private static final String PROP_NODE_SPECIFIER_INPUT = "nodes";

  @Nonnull private final String _filterSpecifierFactory;

  @Nullable private String _filterSpecifierInput;

  private final boolean _ignoreComposites;

  @Nonnull private String _nodeSpecifierFactory;

  @Nullable private String _nodeSpecifierInput;

  @JsonCreator
  private static FilterLineReachabilityQuestion create(
      @Nullable @JsonProperty(PROP_FILTER_SPECIFIER_FACTORY) String filterSpecifierFactory,
      @Nullable @JsonProperty(PROP_FILTER_SPECIFIER_INPUT) String filtersSpecifierInput,
      @Nullable @JsonProperty(PROP_NODE_SPECIFIER_FACTORY) String nodeSpecifierFactory,
      @Nullable @JsonProperty(PROP_NODE_SPECIFIER_INPUT) String nodeSpecifierInput,
      @Nullable @JsonProperty(PROP_IGNORE_COMPOSITES) Boolean ignoreComposites) {
    return new FilterLineReachabilityQuestion(
        filterSpecifierFactory,
        filtersSpecifierInput,
        nodeSpecifierFactory,
        nodeSpecifierInput,
        ignoreComposites);
  }

  @VisibleForTesting
  FilterLineReachabilityQuestion() {
    this(null, null, null, null, null);
  }

  @VisibleForTesting
  FilterLineReachabilityQuestion(String filterSpecifierInput) {
    this(null, filterSpecifierInput, null, null, null);
  }

  public FilterLineReachabilityQuestion(
      @Nullable String filterSpecifierInput,
      @Nullable String nodeSpecifierInput,
      boolean ignoreComposites) {
    this(null, filterSpecifierInput, null, nodeSpecifierInput, ignoreComposites);
  }

  public FilterLineReachabilityQuestion(
      @Nullable String filterSpecifierFactory,
      @Nullable String filtersSpecifierInput,
      @Nullable String nodeSpecifierFactory,
      @Nullable String nodeSpecifierInput,
      @Nullable Boolean ignoreComposites) {
    _filterSpecifierFactory =
        firstNonNull(filterSpecifierFactory, DEFAULT_FILTER_SPECIFIER_FACTORY);
    _filterSpecifierInput = filtersSpecifierInput;
    _nodeSpecifierFactory = firstNonNull(nodeSpecifierFactory, DEFAULT_NODE_SPECIFIER_FACTORY);
    _nodeSpecifierInput = nodeSpecifierInput;
    _ignoreComposites = firstNonNull(ignoreComposites, DEFAULT_IGNORE_COMPOSITES);
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Nonnull
  @JsonProperty(PROP_FILTER_SPECIFIER_FACTORY)
  public String getFilterSpecifierFactory() {
    return _filterSpecifierFactory;
  }

  @Nullable
  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  public String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @JsonProperty(PROP_IGNORE_COMPOSITES)
  public boolean getIgnoreComposites() {
    return _ignoreComposites;
  }

  @Override
  public String getName() {
    return "filterLineReachability";
  }

  @Nonnull
  @JsonProperty(PROP_NODE_SPECIFIER_FACTORY)
  public String getNodeSpecifierFactory() {
    return _nodeSpecifierFactory;
  }

  @Nullable
  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  public String getNodeSpecifierInput() {
    return _nodeSpecifierInput;
  }

  @Nonnull
  @JsonIgnore
  FilterSpecifier filterSpecifier() {
    return FilterSpecifierFactory.load(_filterSpecifierFactory)
        .buildFilterSpecifier(_filterSpecifierInput);
  }

  @Nonnull
  @JsonIgnore
  NodeSpecifier nodeSpecifier() {
    return NodeSpecifierFactory.load(_nodeSpecifierFactory).buildNodeSpecifier(_nodeSpecifierInput);
  }
}
