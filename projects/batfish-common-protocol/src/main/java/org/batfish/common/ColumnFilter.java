package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.table.Row;

public final class ColumnFilter {
  private static final String PROP_COLUMN = "column";
  private static final String PROP_FILTER_TEXT = "filterText";

  @JsonCreator
  private static @Nonnull ColumnFilter create(
      @JsonProperty(PROP_COLUMN) String column, @JsonProperty(PROP_FILTER_TEXT) String filterText) {
    return new ColumnFilter(requireNonNull(column), firstNonNull(filterText, ""));
  }

  private final String _column;

  private final String _filterText;

  public ColumnFilter(@Nonnull String column, @Nonnull String filterText) {
    _column = column;
    _filterText = filterText;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ColumnFilter)) {
      return false;
    }
    ColumnFilter rhs = (ColumnFilter) obj;
    return _column.equals(rhs._column) && _filterText.equals(rhs._filterText);
  }

  @JsonProperty(PROP_COLUMN)
  public @Nonnull String getColumn() {
    return _column;
  }

  @JsonProperty(PROP_FILTER_TEXT)
  public @Nonnull String getFilterText() {
    return _filterText;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_column, _filterText);
  }

  public boolean matches(@Nonnull Row row) {
    return row.get(_column).toString().toLowerCase().contains(_filterText.toLowerCase());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_COLUMN, _column)
        .add(PROP_FILTER_TEXT, _filterText)
        .toString();
  }
}
