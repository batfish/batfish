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
import org.batfish.common.BfConsts;

public class Metrics {

  @JsonCreator
  private static @Nonnull Metrics create(
      @JsonProperty(BfConsts.PROP_AGGREGATIONS) List<ColumnAggregationResult> aggregations,
      @JsonProperty(BfConsts.PROP_NUM_ROWS) int numRows) {
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

  @JsonProperty(BfConsts.PROP_AGGREGATIONS)
  public @Nonnull List<ColumnAggregationResult> getAggregations() {
    return _aggregations;
  }

  @JsonProperty(BfConsts.PROP_NUM_ROWS)
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
        .add(BfConsts.PROP_AGGREGATIONS, _aggregations)
        .add(BfConsts.PROP_NUM_ROWS, _numRows)
        .toString();
  }
}
