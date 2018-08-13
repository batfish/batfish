package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Metrics {

  public static final String PROP_AGGREGATIONS = "aggregations";

  public static final String PROP_NUM_ROWS = "numRows";

  @JsonCreator
  private static @Nonnull Metrics create(
      @JsonProperty(PROP_AGGREGATIONS) List<ColumnAggregationResult> aggregations,
      @JsonProperty(PROP_NUM_ROWS) int numRows) {
    return new Metrics(firstNonNull(aggregations, ImmutableList.of()), numRows);
  }

  private final List<ColumnAggregationResult> _aggregations;

  private final int _numRows;

  public Metrics(@Nonnull List<ColumnAggregationResult> aggregations, int numRows) {
    _aggregations = aggregations;
    _numRows = numRows;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Metrics)) {
      return false;
    }
    Metrics rhs = (Metrics) obj;
    return _aggregations.equals(rhs._aggregations) && _numRows == rhs._numRows;
  }

  @JsonProperty(PROP_AGGREGATIONS)
  public @Nonnull List<ColumnAggregationResult> getAggregations() {
    return _aggregations;
  }

  @JsonProperty(PROP_NUM_ROWS)
  public int getNumRows() {
    return _numRows;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aggregations, _numRows);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_AGGREGATIONS, _aggregations)
        .add(PROP_NUM_ROWS, _numRows)
        .toString();
  }
}
