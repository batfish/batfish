package org.batfish.question.filterlinereachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.ShorthandFilterSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * A question that returns unreachable lines of ACLs in a tabular format. {@link
 * FilterLineReachabilityQuestion#_filterSpecifierInput} determines which ACLs are checked, and
 * {@link FilterLineReachabilityQuestion#_nodeSpecifierInput} determines which nodes are checked for
 * those ACLs.
 */
@ParametersAreNonnullByDefault
public class FilterLineReachabilityQuestion extends Question {
  private static final boolean DEFAULT_IGNORE_COMPOSITES = true;

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filters";

  private static final String PROP_IGNORE_COMPOSITES = "ignoreComposites";

  private static final String PROP_NODE_SPECIFIER_INPUT = "nodes";

  @Nullable private String _filterSpecifierInput;

  private final boolean _ignoreComposites;

  @Nullable private String _nodeSpecifierInput;

  @JsonCreator
  private static FilterLineReachabilityQuestion create(
      @Nullable @JsonProperty(PROP_FILTER_SPECIFIER_INPUT) String filtersSpecifierInput,
      @Nullable @JsonProperty(PROP_NODE_SPECIFIER_INPUT) String nodeSpecifierInput,
      @Nullable @JsonProperty(PROP_IGNORE_COMPOSITES) Boolean ignoreComposites) {
    return new FilterLineReachabilityQuestion(
        filtersSpecifierInput, nodeSpecifierInput, ignoreComposites);
  }

  @VisibleForTesting
  FilterLineReachabilityQuestion() {
    this(null, null, null);
  }

  @VisibleForTesting
  FilterLineReachabilityQuestion(String filterSpecifierInput) {
    this(filterSpecifierInput, null, null);
  }

  public FilterLineReachabilityQuestion(
      @Nullable String filtersSpecifierInput,
      @Nullable String nodeSpecifierInput,
      @Nullable Boolean ignoreComposites) {
    _filterSpecifierInput = filtersSpecifierInput;
    _nodeSpecifierInput = nodeSpecifierInput;
    _ignoreComposites = firstNonNull(ignoreComposites, DEFAULT_IGNORE_COMPOSITES);
  }

  @Override
  public boolean getDataPlane() {
    return false;
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

  @Nullable
  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  public String getNodeSpecifierInput() {
    return _nodeSpecifierInput;
  }

  @Nonnull
  @JsonIgnore
  FilterSpecifier filterSpecifier() {
    return SpecifierFactories.getFilterSpecifierOrDefault(
        _filterSpecifierInput, new ShorthandFilterSpecifier(FiltersSpecifier.ALL));
  }

  @Nonnull
  @JsonIgnore
  NodeSpecifier nodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(
        _nodeSpecifierInput, AllNodesNodeSpecifier.INSTANCE);
  }
}
