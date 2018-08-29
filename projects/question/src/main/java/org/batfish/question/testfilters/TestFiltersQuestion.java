package org.batfish.question.testfilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.IPacketTraceQuestion;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;

/**
 * Computes the fate of the flow at a filter. The set of filters to consider are controlled by node
 * and filter regex fields. By default, all filters on all nodes are considered.
 */
public class TestFiltersQuestion extends IPacketTraceQuestion {

  private static final String FILTER_SPECIFIER_FACTORY = FlexibleFilterSpecifierFactory.NAME;

  private static final String PROP_FILTERS = "filters";

  private static final String PROP_NODES = "nodes";

  @Nullable private String _filterSpecifierInput;

  @Nonnull private NodesSpecifier _nodes;

  @JsonCreator
  public TestFiltersQuestion(
      @JsonProperty(PROP_NODES) NodesSpecifier nodes,
      @JsonProperty(PROP_FILTERS) String filterSpecifierInput) {
    _nodes = nodes == null ? NodesSpecifier.ALL : nodes;
    _filterSpecifierInput = filterSpecifierInput;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTERS)
  private String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @Nonnull
  @JsonIgnore
  public FilterSpecifier getFilterSpecifier() {
    return FilterSpecifierFactory.load(FILTER_SPECIFIER_FACTORY)
        .buildFilterSpecifier(_filterSpecifierInput);
  }

  @Override
  public String getName() {
    return "testFilters";
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }
}
