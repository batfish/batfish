package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Metrics {

  public static final String PROP_AGGREGATIONS = "aggregations";

  public static final String PROP_NUM_ROWS = "numRows";

  private final List<ColumnAggregationResult> _aggregations;

  private final int _numRows;

  @JsonCreator
  public Metrics(
      @JsonProperty(PROP_AGGREGATIONS) List<ColumnAggregationResult> aggregations,
      @JsonProperty(PROP_NUM_ROWS) int numRows) {
    _aggregations = aggregations;
    _numRows = numRows;
  }

  @JsonProperty(PROP_AGGREGATIONS)
  public List<ColumnAggregationResult> getAggregations() {
    return _aggregations;
  }

  @JsonProperty(PROP_NUM_ROWS)
  public int getNumRows() {
    return _numRows;
  }
}
