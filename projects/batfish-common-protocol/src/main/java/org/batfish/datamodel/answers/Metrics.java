package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BfConsts;

public class Metrics {

  @JsonCreator
  private static @Nonnull Metrics create(
      @JsonProperty(BfConsts.PROP_AGGREGATIONS) Map<String, Map<Aggregation, Object>> aggregations,
      @JsonProperty(BfConsts.PROP_EMPTY_COLUMNS) Set<String> emptyColumns,
      @JsonProperty(BfConsts.PROP_NUM_ROWS) int numRows) {
    return new Metrics(
        firstNonNull(aggregations, ImmutableMap.of()),
        firstNonNull(emptyColumns, ImmutableSet.of()),
        numRows);
  }

  private final Map<String, Map<Aggregation, Object>> _aggregations;

  private final Set<String> _emptyColumns;

  private final int _numRows;

  public Metrics(
      @Nonnull Map<String, Map<Aggregation, Object>> aggregations,
      @Nonnull Set<String> emptyColumns,
      int numRows) {
    _aggregations = aggregations;
    _emptyColumns = emptyColumns;
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
    return _aggregations.equals(rhs._aggregations)
        && _emptyColumns.equals(rhs._emptyColumns)
        && _numRows == rhs._numRows;
  }

  @JsonProperty(BfConsts.PROP_AGGREGATIONS)
  public @Nonnull Map<String, Map<Aggregation, Object>> getAggregations() {
    return _aggregations;
  }

  @JsonProperty(BfConsts.PROP_EMPTY_COLUMNS)
  public Set<String> getEmptyColumns() {
    return _emptyColumns;
  }

  @JsonProperty(BfConsts.PROP_NUM_ROWS)
  public int getNumRows() {
    return _numRows;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aggregations, _emptyColumns, _numRows);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(BfConsts.PROP_AGGREGATIONS, _aggregations)
        .add(BfConsts.PROP_EMPTY_COLUMNS, _emptyColumns)
        .add(BfConsts.PROP_NUM_ROWS, _numRows)
        .toString();
  }
}
