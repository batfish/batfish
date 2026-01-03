package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AnswerRowsOptions {

  @JsonCreator
  @VisibleForTesting
  static @Nonnull AnswerRowsOptions create(
      @JsonProperty(BfConsts.PROP_COLUMNS) Set<String> columns,
      @JsonProperty(BfConsts.PROP_FILTERS) List<ColumnFilter> filters,
      @JsonProperty(BfConsts.PROP_MAX_ROWS) Integer maxRows,
      @JsonProperty(BfConsts.PROP_ROW_OFFSET) Integer rowOffset,
      @JsonProperty(BfConsts.PROP_SORT_ORDER) List<ColumnSortOption> sortOrder,
      @JsonProperty(BfConsts.PROP_UNIQUE_ROWS) Boolean uniqueRows) {
    return new AnswerRowsOptions(
        firstNonNull(columns, ImmutableSet.of()),
        firstNonNull(filters, ImmutableList.of()),
        firstNonNull(maxRows, Integer.MAX_VALUE),
        firstNonNull(rowOffset, 0),
        firstNonNull(sortOrder, ImmutableList.of()),
        firstNonNull(uniqueRows, false));
  }

  /** Filter which returns all answer rows and columns */
  public static @Nonnull AnswerRowsOptions NO_FILTER =
      AnswerRowsOptions.create(null, null, null, null, null, null);

  private final Set<String> _columns;

  private final List<ColumnFilter> _filters;

  private final int _maxRows;

  private final int _rowOffset;

  private final List<ColumnSortOption> _sortOrder;

  private final boolean _uniqueRows;

  public AnswerRowsOptions(
      @Nonnull Set<String> columns,
      @Nonnull List<ColumnFilter> filters,
      int maxRows,
      int rowOffset,
      @Nonnull List<ColumnSortOption> sortOrder,
      boolean uniqueRows) {
    _columns = columns;
    _filters = filters;
    _maxRows = maxRows;
    _rowOffset = rowOffset;
    _sortOrder = sortOrder;
    _uniqueRows = uniqueRows;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AnswerRowsOptions)) {
      return false;
    }
    AnswerRowsOptions rhs = (AnswerRowsOptions) obj;
    return _columns.equals(rhs._columns)
        && _filters.equals(rhs._filters)
        && _maxRows == rhs._maxRows
        && _rowOffset == rhs._rowOffset
        && _sortOrder.equals(rhs._sortOrder)
        && _uniqueRows == rhs._uniqueRows;
  }

  @JsonProperty(BfConsts.PROP_COLUMNS)
  public Set<String> getColumns() {
    return _columns;
  }

  @JsonProperty(BfConsts.PROP_FILTERS)
  public @Nonnull List<ColumnFilter> getFilters() {
    return _filters;
  }

  @JsonProperty(BfConsts.PROP_MAX_ROWS)
  public int getMaxRows() {
    return _maxRows;
  }

  @JsonProperty(BfConsts.PROP_ROW_OFFSET)
  public int getRowOffset() {
    return _rowOffset;
  }

  @JsonProperty(BfConsts.PROP_SORT_ORDER)
  public List<ColumnSortOption> getSortOrder() {
    return _sortOrder;
  }

  @JsonProperty(BfConsts.PROP_UNIQUE_ROWS)
  public boolean getUniqueRows() {
    return _uniqueRows;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_columns, _filters, _maxRows, _rowOffset, _sortOrder, _uniqueRows);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(BfConsts.PROP_COLUMNS, _columns)
        .add(BfConsts.PROP_FILTERS, _filters)
        .add(BfConsts.PROP_MAX_ROWS, _maxRows)
        .add(BfConsts.PROP_ROW_OFFSET, _rowOffset)
        .add(BfConsts.PROP_SORT_ORDER, _sortOrder)
        .add(BfConsts.PROP_UNIQUE_ROWS, _uniqueRows)
        .toString();
  }
}
