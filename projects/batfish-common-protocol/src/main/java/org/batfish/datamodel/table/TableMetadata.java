package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.DisplayHints;

public class TableMetadata {

  private static final String PROP_COLUMN_METADATA = "columnMetadata";

  private static final String PROP_DISPLAY_HINTS = "displayHints";

  private static final String PROP_TEXT_DESC = "textDesc";

  @Nonnull private List<ColumnMetadata> _columnMetadata;

  @Nullable private String _textDesc;

  public TableMetadata(@Nullable List<ColumnMetadata> columnMetadata) {
    this(columnMetadata, (String) null);
  }

  public TableMetadata(@Nullable List<ColumnMetadata> columnMetadata, @Nullable String textDesc) {
    _columnMetadata = ImmutableList.copyOf(firstNonNull(columnMetadata, ImmutableList.of()));
    _textDesc = textDesc;

    // check if there is a duplicate column name
    Set<String> duplicateCheckSet = new HashSet<>();
    for (ColumnMetadata cm : _columnMetadata) {
      if (!duplicateCheckSet.add(cm.getName())) {
        throw new IllegalArgumentException(
            "Cannot have two columns with the same name '" + cm.getName() + "'");
      }
    }
  }

  @Deprecated
  public TableMetadata(
      @Nullable List<ColumnMetadata> columnMetadata, @Nullable DisplayHints displayHints) {
    this(columnMetadata, displayHints != null ? displayHints.getTextDesc() : null);
  }

  @JsonCreator
  private static TableMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_COLUMN_METADATA) List<ColumnMetadata> columnMetadata,
      @Nullable @JsonProperty(PROP_DISPLAY_HINTS) DisplayHints displayHints,
      @Nullable @JsonProperty(PROP_TEXT_DESC) String textDesc) {
    String usedTextDesc = textDesc;
    if (usedTextDesc == null && displayHints != null) {
      usedTextDesc = displayHints.getTextDesc();
    }

    return new TableMetadata(columnMetadata, usedTextDesc);
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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TableMetadata)) {
      return false;
    }
    return Objects.equals(_columnMetadata, ((TableMetadata) o)._columnMetadata)
        && Objects.equals(_textDesc, ((TableMetadata) o)._textDesc);
  }

  @JsonProperty(PROP_COLUMN_METADATA)
  public List<ColumnMetadata> getColumnMetadata() {
    return _columnMetadata;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public String getTextDesc() {
    return _textDesc;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_columnMetadata, _textDesc);
  }

  @Override
  public String toString() {
    return _columnMetadata.toString() + (_textDesc != null ? " " + _textDesc : "");
  }

  /** Returns a map from column name to {@link ColumnMetadata} */
  public Map<String, ColumnMetadata> toColumnMap() {
    return _columnMetadata
        .stream()
        .collect(ImmutableMap.toImmutableMap(ColumnMetadata::getName, cm -> cm));
  }
}
