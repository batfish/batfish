package org.batfish.question.tracefilters;

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
public class TraceFiltersQuestion extends IPacketTraceQuestion {

  private static final String FILTER_SPECIFIER_FACTORY = FlexibleFilterSpecifierFactory.NAME;

  private static final String PROP_FILTER_REGEX = "filterRegex";

  private static final String PROP_NODE_REGEX = "nodeRegex";

  @Nullable private String _filterRegex;

  @Nonnull private NodesSpecifier _nodeRegex;

  @JsonCreator
  public TraceFiltersQuestion(
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_FILTER_REGEX) String filterRegex) {
    _nodeRegex = nodeRegex == null ? NodesSpecifier.ALL : nodeRegex;
    _filterRegex = filterRegex;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTER_REGEX)
  public String getFilterRegex() {
    return _filterRegex;
  }

  @Nonnull
  @JsonIgnore
  public FilterSpecifier getFilterSpecifier() {
    return FilterSpecifierFactory.load(FILTER_SPECIFIER_FACTORY).buildFilterSpecifier(_filterRegex);
  }

  @Override
  public String getName() {
    return "tracefilters";
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }
}
