package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnAggregationResult {

  public static final String PROP_AGGREGATION = "aggregation";

  public static final String PROP_COLUMN = "column";

  public static final String PROP_VALUE = "value";

  private final Aggregation _aggregation;

  private final String _column;

  private final Object _value;

  @JsonCreator
  public ColumnAggregationResult(
      @JsonProperty(PROP_AGGREGATION) Aggregation aggregation,
      @JsonProperty(PROP_COLUMN) String column,
      @JsonProperty(PROP_VALUE) Object value) {
    _aggregation = aggregation;
    _column = column;
    _value = value;
  }

  @JsonProperty(PROP_AGGREGATION)
  public Aggregation getAggregation() {
    return _aggregation;
  }

  @JsonProperty(PROP_COLUMN)
  public String getColumn() {
    return _column;
  }

  @JsonProperty(PROP_VALUE)
  public Object getValue() {
    return _value;
  }
}
