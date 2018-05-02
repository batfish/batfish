package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents rows that have been excludes from {@link TableAnswerElement} because they were covered
 * by an exclusion.
 */
public class ExcludedRows {

  private static final String PROP_EXCLUSION_NAME = "exclusionName";

  private static final String PROP_ROWS = "rows";

  @Nonnull final String _exclusionName;

  @Nonnull final Rows _rows;

  @JsonCreator
  public ExcludedRows(
      @Nonnull @JsonProperty(PROP_EXCLUSION_NAME) String exclusionName,
      @Nullable @JsonProperty(PROP_ROWS) Rows rows) {
    _exclusionName = exclusionName;
    _rows = rows == null ? new Rows() : rows;
  }

  public void addRow(Row row) {
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
