package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.AnswerElement;

public class TableAnswerElement extends AnswerElement {

  protected static final String PROP_EXCLUDED_ROWS = "excludedRows";

  protected static final String PROP_METADATA = "metadata";

  protected static final String PROP_ROWS = "rows";

  Map<String, Rows> _excludedRows;

  TableMetadata _tableMetadata;

  Rows _rows;

  @JsonCreator
  public TableAnswerElement(@Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    _tableMetadata = tableMetadata;
    _rows = new Rows();
    _excludedRows = new HashMap<>();
  }

  public void addRow(ObjectNode row) {
    _rows.add(row);
  }

  public void addExcludedRow(ObjectNode row, ObjectNode exclusion) {
    Rows exclusionRows = _excludedRows.computeIfAbsent(exclusion.toString(), e -> new Rows());
    exclusionRows.add(row);
  }

  @JsonProperty(PROP_EXCLUDED_ROWS)
  public Map<String, Rows> getExcludedRows() {
    return _excludedRows;
  }

  @JsonProperty(PROP_METADATA)
  public TableMetadata getMetadata() {
    return _tableMetadata;
  }

  @JsonProperty(PROP_ROWS)
  public Rows getRows() {
    return _rows;
  }

  @JsonProperty(PROP_EXCLUDED_ROWS)
  private void setExcludedRows(Map<String, Rows> excludedRows) {
    _excludedRows = excludedRows == null ? new HashMap<>() : excludedRows;
  }

  @JsonProperty(PROP_ROWS)
  private void setRows(Rows rows) {
    _rows = rows == null ? new Rows() : rows;
  }
}
