package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.IPacketTraceQuestion;
import org.batfish.datamodel.questions.NodesSpecifier;

// <question_page_comment>

/**
 * Checks if IPSec VPNs are correctly configured.
 *
 * <p>Details coming on what it means to be correctly configured.
 *
 * @type IpsecVpnStatus multifile
 * @param nodeRegex NodesSpecifier expression to match the nodes. Default is '.*' (all nodes).
 * @param filterRegex FiltersSpecifier to match the filters. Default is '.*' (all filters).
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
