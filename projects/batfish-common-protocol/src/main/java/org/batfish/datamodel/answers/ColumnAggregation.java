package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnAggregation {

  public static final String PROP_AGGREGATION = "aggregation";

  public static final String PROP_COLUMN = "column";

  private final Aggregation _aggregation;

  private final String _column;

  @JsonCreator
  public ColumnAggregation(
      @JsonProperty(PROP_AGGREGATION) Aggregation aggregation,
      @JsonProperty(PROP_COLUMN) String column) {
    _aggregation = aggregation;
    _column = column;
  }

  @JsonProperty(PROP_AGGREGATION)
  public Aggregation getAggregation() {
    return _aggregation;
  }

  @JsonProperty(PROP_COLUMN)
  public String getColumn() {
    return _column;
  }
}
