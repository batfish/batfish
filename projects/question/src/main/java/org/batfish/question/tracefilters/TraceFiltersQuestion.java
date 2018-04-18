package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.FiltersSpecifier.Type;
import org.batfish.datamodel.questions.IPacketTraceQuestion;
import org.batfish.datamodel.questions.NodesSpecifier;

/**
 * Computes the fate of the flow at a filter. The set of filters to consider are controlled by node
 * and filter regex fields. By default, all filters on all nodes are considered.
 */
public class TraceFiltersQuestion extends IPacketTraceQuestion {

  private static final String PROP_FILTER_REGEX = "filterRegex";

  private static final String PROP_NODE_REGEX = "nodeRegex";

  @Nonnull private FiltersSpecifier _filterRegex;

  @Nonnull private NodesSpecifier _nodeRegex;

  @JsonCreator
  public TraceFiltersQuestion(
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @JsonProperty(PROP_FILTER_REGEX) FiltersSpecifier filterRegex) {
    _nodeRegex = nodeRegex == null ? NodesSpecifier.ALL : nodeRegex;
    _filterRegex = filterRegex == null ? FiltersSpecifier.ALL : filterRegex;
    if (_filterRegex.getType() == Type.IPV6) {
      throw new IllegalArgumentException("IPV6 filters are not currently supported");
    }
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTER_REGEX)
  public FiltersSpecifier getFilterRegex() {
    return _filterRegex;
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
