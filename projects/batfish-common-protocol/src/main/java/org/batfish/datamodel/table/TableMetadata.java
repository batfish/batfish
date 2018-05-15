package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.DisplayHints;

public class TableMetadata {

  private static final String PROP_COLUMN_METADATA = "columnMetadata";

  private static final String PROP_DISPLAY_HINTS = "displayHints";

  @Nonnull private List<ColumnMetadata> _columnMetadata;

  @Nullable private DisplayHints _displayHints;

  public TableMetadata() {
    this(null, null);
  }

  @JsonCreator
  public TableMetadata(
      @Nullable @JsonProperty(PROP_COLUMN_METADATA) List<ColumnMetadata> columnMetadata,
      @Nullable @JsonProperty(PROP_DISPLAY_HINTS) DisplayHints displayHints) {
    _columnMetadata = ImmutableList.copyOf(firstNonNull(columnMetadata, new LinkedList<>()));
    _displayHints = displayHints;

    // check is there is a duplicate column names
    Set<String> duplicateCheckSet = new HashSet<>();
    String duplicateName = null;
    for (ColumnMetadata cm : _columnMetadata) {
      if (!duplicateCheckSet.add(cm.getName())) {
        duplicateName = cm.getName();
        break;
      }
    }
    if (duplicateName != null) {
      throw new IllegalArgumentException(
          "Cannot have two columns with the same name '" + duplicateName + "'");
    }
  }

  /**
   * Does this table metadata contain a column by this name?
   *
   * @param columnName The name of the column to check
   * @return Whether a column by this name exists
   */
  public boolean containsColumn(String columnName) {
    if (columnName == null) {
      return false;
    }
    return _columnMetadata.stream().anyMatch(cm -> columnName.equals(cm.getName()));
  }

  @JsonProperty(PROP_COLUMN_METADATA)
  public List<ColumnMetadata> getColumnMetadata() {
    return _columnMetadata;
  }

  @JsonProperty(PROP_DISPLAY_HINTS)
  public DisplayHints getTextDesc() {
    return _displayHints;
  }
}
