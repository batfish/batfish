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

  public static class Builder {
    private Map<String, Map<Aggregation, Object>> _aggregations;

    private Set<String> _emptyColumns;

    private int _numExcludedRows;

    private int _numRows;

    private Builder() {
      _aggregations = ImmutableMap.of();
      _emptyColumns = ImmutableSet.of();
    }

    public @Nonnull Metrics build() {
      return new Metrics(_aggregations, _emptyColumns, _numExcludedRows, _numRows);
    }

    public @Nonnull Builder setAggregations(
        @Nonnull Map<String, Map<Aggregation, Object>> aggregations) {
      _aggregations = ImmutableMap.copyOf(aggregations);
      return this;
    }

    public @Nonnull Builder setEmptyColumns(@Nonnull Set<String> emptyColumns) {
      _emptyColumns = ImmutableSet.copyOf(emptyColumns);
      return this;
    }

    public @Nonnull Builder setNumExcludedRows(int numExcludedRows) {
      _numExcludedRows = numExcludedRows;
      return this;
    }

    public @Nonnull Builder setNumRows(int numRows) {
      _numRows = numRows;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull Metrics create(
      @JsonProperty(BfConsts.PROP_AGGREGATIONS) Map<String, Map<Aggregation, Object>> aggregations,
      @JsonProperty(BfConsts.PROP_EMPTY_COLUMNS) Set<String> emptyColumns,
      @JsonProperty(BfConsts.PROP_NUM_EXCLUDED_ROWS) int numExcludedRows,
      @JsonProperty(BfConsts.PROP_NUM_ROWS) int numRows) {
    return new Metrics(
        firstNonNull(aggregations, ImmutableMap.of()),
        firstNonNull(emptyColumns, ImmutableSet.of()),
        numExcludedRows,
        numRows);
  }

  private final Map<String, Map<Aggregation, Object>> _aggregations;

  private final Set<String> _emptyColumns;

  private final int _numExcludedRows;

  private final int _numRows;

  private Metrics(
      @Nonnull Map<String, Map<Aggregation, Object>> aggregations,
      @Nonnull Set<String> emptyColumns,
      int numExcludedRows,
      int numRows) {
    _aggregations = aggregations;
    _emptyColumns = emptyColumns;
    _numExcludedRows = numExcludedRows;
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
        && _numExcludedRows == rhs._numExcludedRows
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

  @JsonProperty(BfConsts.PROP_NUM_EXCLUDED_ROWS)
  public int getNumExcludedRows() {
    return _numExcludedRows;
  }

  @JsonProperty(BfConsts.PROP_NUM_ROWS)
  public int getNumRows() {
    return _numRows;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aggregations, _emptyColumns, _numExcludedRows, _numRows);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(BfConsts.PROP_AGGREGATIONS, _aggregations)
        .add(BfConsts.PROP_EMPTY_COLUMNS, _emptyColumns)
        .add(BfConsts.PROP_NUM_EXCLUDED_ROWS, _numExcludedRows)
        .add(BfConsts.PROP_NUM_ROWS, _numRows)
        .toString();
  }
}
