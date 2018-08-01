package org.batfish.question.reachfilter;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.DENY;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.MATCH_LINE;
import static org.batfish.question.reachfilter.ReachFilterQuestion.Type.PERMIT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;

/** A question to check for multipath inconsistencies. */
public class ReachFilterQuestion extends Question {

  private static final String FILTER_SPECIFIER_FACTORY = FlexibleFilterSpecifierFactory.NAME;

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filterRegex";

  private static final String PROP_NODES_SPECIFIER_NAME = "nodeRegex";

  private static final String PROP_QUERY = "query";

  public enum Type {
    PERMIT,
    DENY,
    MATCH_LINE
  }

  // Invariant: null unless _type == MATCH_LINE
  @Nullable private Integer _lineNumber;

  @Nullable private String _filterSpecifierInput;

  @Nonnull private NodesSpecifier _nodesSpecifier = NodesSpecifier.ALL;

  private Type _type = PERMIT;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "reachfilter";
  }

  @Nonnull
  @JsonIgnore
  public FilterSpecifier getFilterSpecifier() {
    return FilterSpecifierFactory.load(FILTER_SPECIFIER_FACTORY)
        .buildFilterSpecifier(_filterSpecifierInput);
  }

  @Nullable
  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  private String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @Nonnull
  @JsonProperty(PROP_NODES_SPECIFIER_NAME)
  public NodesSpecifier getNodesSpecifier() {
    return _nodesSpecifier;
  }

  @JsonIgnore
  @Nullable
  public Integer getLineNumber() {
    return _lineNumber;
  }

  @JsonIgnore
  public Type getType() {
    return _type;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  public void setFilterSpecifierInput(@Nullable String filterSpecifierInput) {
    _filterSpecifierInput = filterSpecifierInput;
  }

  @JsonProperty(PROP_NODES_SPECIFIER_NAME)
  public void setNodesSpecifier(@Nullable NodesSpecifier nodesSpecifier) {
    _nodesSpecifier = firstNonNull(nodesSpecifier, NodesSpecifier.ALL);
  }

  @JsonProperty(PROP_QUERY)
  public void setQuery(String query) {
    if (query.equals("permit")) {
      _type = PERMIT;
      _lineNumber = null;
    } else if (query.equals("deny")) {
      _type = DENY;
      _lineNumber = null;
    } else if (query.startsWith("matchLine")) {
      _type = MATCH_LINE;
      _lineNumber = Integer.parseInt(query.substring("matchLine".length()).trim());
    } else {
      throw new BatfishException("Unrecognized query: " + query);
    }
  }
}
