package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.AnswerElement;

/** A base class for tabular answers */
public class TableAnswerElement extends AnswerElement {

  protected static final String PROP_EXCLUDED_ROWS = "excludedRows";

  protected static final String PROP_METADATA = "metadata";

  protected static final String PROP_ROWS = "rows";

  List<ExcludedRows> _excludedRows;

  Rows _rows;

  TableMetadata _tableMetadata;

  transient Map<String, ExcludedRows> _exclusionMap;

  @JsonCreator
  public TableAnswerElement(@Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    _tableMetadata = tableMetadata;
    _rows = new Rows();
    _excludedRows = new LinkedList<>();
    _exclusionMap = new HashMap<>();
  }

  /**
   * Adds a new row to data rows
   *
   * @param row The row to add
   */
  public void addRow(ObjectNode row) {
    _rows.add(row);
  }

  /**
   * Adds a new row to excluded data rows
   *
   * @param row The row to add
   */
  public void addExcludedRow(ObjectNode row, String exclusionName) {
    ExcludedRows rows = null;
    if (!_exclusionMap.containsKey(exclusionName)) {
      rows = new ExcludedRows(exclusionName, new Rows());
      _exclusionMap.put(exclusionName, rows);
      _excludedRows.add(rows);
    }
    rows = _exclusionMap.get(exclusionName);
    rows.addRow(row);
  }

  @JsonProperty(PROP_EXCLUDED_ROWS)
  public List<ExcludedRows> getExcludedRows() {
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
  private void setExcludedRows(List<ExcludedRows> excludedRows) {
    _excludedRows = excludedRows == null ? new LinkedList<>() : excludedRows;
  }

  @JsonProperty(PROP_ROWS)
  private void setRows(Rows rows) {
    _rows = rows == null ? new Rows() : rows;
  }
}
