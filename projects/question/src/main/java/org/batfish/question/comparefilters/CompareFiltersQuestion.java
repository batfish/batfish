package org.batfish.question.comparefilters;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.Question;

/** A question to compare the flows permitted/denied by two filters. */
@ParametersAreNonnullByDefault
public final class CompareFiltersQuestion extends Question {
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_NODES = "nodes";

  private static final String DEFAULT_FILTERS = ".*";
  private static final String DEFAULT_NODES = ".*";

  private final String _filters;
  private final String _nodes;

  CompareFiltersQuestion() {
    this(DEFAULT_FILTERS, DEFAULT_NODES);
  }

  CompareFiltersQuestion(String filters, String nodes) {
    _filters = filters;
    _nodes = nodes;
  }

  @JsonCreator
  private static CompareFiltersQuestion jsonCreator(
      @JsonProperty(PROP_FILTERS) @Nullable String filters,
      @JsonProperty(PROP_NODES) @Nullable String nodes) {
    return new CompareFiltersQuestion(
        firstNonNull(filters, DEFAULT_FILTERS), firstNonNull(nodes, DEFAULT_NODES));
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "compareFilters";
  }

  @JsonProperty(PROP_FILTERS)
  public String getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }
}
