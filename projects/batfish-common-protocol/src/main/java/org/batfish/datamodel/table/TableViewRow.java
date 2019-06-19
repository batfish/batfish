package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class TableViewRow {
  private static final String PROP_ID = "id";
  private static final String PROP_ROW = "row";

  @JsonCreator
  private static @Nonnull TableViewRow create(
      @JsonProperty(PROP_ID) @Nullable Integer id, @JsonProperty(PROP_ROW) @Nullable Row row) {
    checkArgument(id != null, "Missing %s", PROP_ID);
    checkArgument(row != null, "Missing %s", PROP_ROW);
    return new TableViewRow(id, row);
  }

  private final int _id;

  private final Row _row;

  public TableViewRow(int id, Row row) {
    _id = id;
    _row = row;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TableViewRow)) {
      return false;
    }
    TableViewRow rhs = (TableViewRow) obj;
    return _id == rhs._id && _row.equals(rhs._row);
  }

  @JsonProperty(PROP_ID)
  public int getId() {
    return _id;
  }

  @JsonProperty(PROP_ROW)
  public @Nonnull Row getRow() {
    return _row;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _row);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_ID, _id).add(PROP_ROW, _row).toString();
  }
}
