package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.annotation.Nonnull;

public class ExcludedRows {

  private static final String PROP_EXCLUSION_NAME = "exclusionName";

  private static final String PROP_ROWS = "rows";

  @Nonnull final String _exclusionName;

  @Nonnull final Rows _rows;

  @JsonCreator
  public ExcludedRows(
      @Nonnull @JsonProperty(PROP_EXCLUSION_NAME) String exclusionName,
      @Nonnull @JsonProperty(PROP_ROWS) Rows rows) {
    _exclusionName = exclusionName;
    _rows = rows;
  }

  public void addRow(ObjectNode row) {
    _rows.add(row);
  }

  @JsonProperty(PROP_EXCLUSION_NAME)
  public String getExclusionName() {
    return _exclusionName;
  }

  @JsonProperty(PROP_ROWS)
  public Rows getRows() {
    return _rows;
  }
}
