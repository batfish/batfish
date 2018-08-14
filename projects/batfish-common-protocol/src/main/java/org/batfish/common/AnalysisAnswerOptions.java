package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnalysisAnswerOptions {

  @JsonCreator
  private static @Nonnull AnalysisAnswerOptions create(
      @JsonProperty(BfConsts.PROP_COLUMNS) Set<String> columns,
      @JsonProperty(BfConsts.PROP_MAX_ROWS) Integer maxRows,
      @JsonProperty(BfConsts.PROP_ROW_OFFSET) Integer rowOffset,
      @JsonProperty(BfConsts.PROP_SORT_ORDER) List<ColumnSortOption> sortOrder) {
    return new AnalysisAnswerOptions(
        firstNonNull(columns, ImmutableSet.of()),
        firstNonNull(maxRows, Integer.MAX_VALUE),
        firstNonNull(rowOffset, 0),
        firstNonNull(sortOrder, ImmutableList.of()));
  }

  private final Set<String> _columns;

  private final int _maxRows;

  private final int _rowOffset;

  private final List<ColumnSortOption> _sortOrder;

  public AnalysisAnswerOptions(
      @Nonnull Set<String> columns,
      int maxRows,
      int rowOffset,
      @Nonnull List<ColumnSortOption> sortOrder) {
    _columns = columns;
    _maxRows = maxRows;
    _rowOffset = rowOffset;
    _sortOrder = sortOrder;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AnalysisAnswerOptions)) {
      return false;
    }
    AnalysisAnswerOptions rhs = (AnalysisAnswerOptions) obj;
    return _columns.equals(rhs._columns)
        && _maxRows == rhs._maxRows
        && _rowOffset == rhs._rowOffset
        && _sortOrder.equals(rhs._sortOrder);
  }

  @JsonProperty(BfConsts.PROP_COLUMNS)
  public Set<String> getColumns() {
    return _columns;
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

  @Override
  public int hashCode() {
    return Objects.hash(_columns, _maxRows, _rowOffset, _sortOrder);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(BfConsts.PROP_COLUMNS, _columns)
        .add(BfConsts.PROP_MAX_ROWS, _maxRows)
        .add(BfConsts.PROP_ROW_OFFSET, _rowOffset)
        .add(BfConsts.PROP_SORT_ORDER, _sortOrder)
        .toString();
  }
}
