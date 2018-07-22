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
import org.batfish.datamodel.questions.FiltersSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/** A question to check for multipath inconsistencies. */
public class ReachFilterQuestion extends Question {

  private static final String PROP_FILTERS_SPECIFIER_NAME = "filterRegex";

  private static final String PROP_NODES_SPECIFIER_NAME = "nodeRegex";

  private static final String PROP_QUERY = "query";

  public enum Type {
    PERMIT,
    DENY,
    MATCH_LINE
  }

  // Invariant: null unless _type == MATCH_LINE
  @Nullable private Integer _lineNumber;

  @Nonnull private FiltersSpecifier _filtersSpecifier = FiltersSpecifier.ALL;

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
  @JsonProperty(PROP_FILTERS_SPECIFIER_NAME)
  public FiltersSpecifier getFiltersSpecifier() {
    return _filtersSpecifier;
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

  @JsonProperty(PROP_FILTERS_SPECIFIER_NAME)
  public void setFiltersSpecifier(@Nullable FiltersSpecifier filtersSpecifier) {
    _filtersSpecifier = firstNonNull(filtersSpecifier, FiltersSpecifier.ALL);
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
