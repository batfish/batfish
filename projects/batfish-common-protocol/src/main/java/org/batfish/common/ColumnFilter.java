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
  private static final String PROP_EXACT = "exact";

  @JsonCreator
  private static @Nonnull ColumnFilter create(
      @JsonProperty(PROP_COLUMN) String column,
      @JsonProperty(PROP_FILTER_TEXT) String filterText,
      @JsonProperty(PROP_EXACT) Boolean exact) {
    return new ColumnFilter(
        requireNonNull(column), firstNonNull(filterText, ""), firstNonNull(exact, false));
  }

  private final @Nonnull String _column;
  private final boolean _exact;
  private final @Nonnull String _filterText;

  public ColumnFilter(@Nonnull String column, @Nonnull String filterText, boolean exact) {
    _column = column;
    _filterText = filterText;
    _exact = exact;
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
    return _column.equals(rhs._column)
        && _filterText.equals(rhs._filterText)
        && _exact == rhs._exact;
  }

  @JsonProperty(PROP_EXACT)
  public boolean getExact() {
    return _exact;
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
    return Objects.hash(_column, _filterText, _exact);
  }

  public boolean matches(@Nonnull Row row) {
    String rowText = row.get(_column).toString();
    if (_exact) {
      // rowText is a stringified JSON type, so strings have extra "", but numbers won't. Accept
      // either.
      return rowText.equalsIgnoreCase(_filterText)
          || rowText.equalsIgnoreCase('"' + _filterText + '"');
    }
    return rowText.toLowerCase().contains(_filterText.toLowerCase());
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_COLUMN, _column)
        .add(PROP_FILTER_TEXT, _filterText)
        .add(PROP_EXACT, _exact)
        .toString();
  }
}
