package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;

public class ColumnSortOption {

  @JsonCreator
  private static @Nonnull ColumnSortOption create(
      @JsonProperty(BfConsts.PROP_COLUMN) String column,
      @JsonProperty(BfConsts.PROP_REVERSED) Boolean reversed) {
    return new ColumnSortOption(requireNonNull(column), firstNonNull(reversed, false));
  }

  private final String _column;

  private final boolean _reversed;

  public ColumnSortOption(@Nonnull String column, boolean reversed) {
    _column = column;
    _reversed = reversed;
  }

  @JsonProperty(BfConsts.PROP_COLUMN)
  public @Nonnull String getColumn() {
    return _column;
  }

  @JsonProperty(BfConsts.PROP_REVERSED)
  public boolean getReversed() {
    return _reversed;
  }
}
