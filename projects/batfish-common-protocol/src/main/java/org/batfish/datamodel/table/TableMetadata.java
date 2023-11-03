package org.batfish.datamodel.table;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.DisplayHints;

/** Represents metadata for a {@link TableAnswerElement} */
@ParametersAreNonnullByDefault
public class TableMetadata {
  private static final String PROP_COLUMN_METADATA = "columnMetadata";
  private static final String PROP_DISPLAY_HINTS = "displayHints";
  private static final String PROP_TEXT_DESC = "textDesc";

  private final @Nonnull List<ColumnMetadata> _columnMetadata;

  private final @Nonnull String _textDesc;

  public TableMetadata(List<ColumnMetadata> columnMetadata) {
    this(columnMetadata, (String) null);
  }

  public TableMetadata(List<ColumnMetadata> columnMetadata, @Nullable String textDesc) {
    checkArgument(columnMetadata != null, "Column metadata cannot be null");
    checkArgument(!columnMetadata.isEmpty(), "The number of columns should be greater than zero");

    // check if there is a duplicate column name
    Set<String> duplicateCheckSet = new HashSet<>();
    for (ColumnMetadata cm : columnMetadata) {
      if (!duplicateCheckSet.add(cm.getName())) {
        throw new IllegalArgumentException(
            "Cannot have two columns with the same name '" + cm.getName() + "'");
      }
    }

    checkArgument(
        columnMetadata.stream().anyMatch(ColumnMetadata::getIsKey),
        "Invalid TableMetadata: no key columns in %s",
        columnMetadata);

    // if textDesc is null, make one up using key columns
    String desc = textDesc;
    if (desc == null) {
      desc =
          columnMetadata.stream()
              .filter(ColumnMetadata::getIsKey)
              .map(cm -> String.format("${%s}", cm.getName()))
              .collect(Collectors.joining(" | "));
    }

    _columnMetadata = columnMetadata;
    _textDesc = desc;
  }

  @Deprecated
  public TableMetadata(List<ColumnMetadata> columnMetadata, @Nullable DisplayHints displayHints) {
    this(columnMetadata, displayHints != null ? displayHints.getTextDesc() : null);
  }

  @JsonCreator
  private static TableMetadata jsonCreator(
      @JsonProperty(PROP_COLUMN_METADATA) @Nullable List<ColumnMetadata> columnMetadata,
      @JsonProperty(PROP_DISPLAY_HINTS) @Nullable DisplayHints displayHints,
      @JsonProperty(PROP_TEXT_DESC) @Nullable String textDesc) {
    return new TableMetadata(
        columnMetadata,
        textDesc == null && displayHints != null ? displayHints.getTextDesc() : textDesc);
  }

  /**
   * Does this table metadata contain a column by this name?
   *
   * @param columnName The name of the column to check
   * @return Whether a column by this name exists
   */
  public boolean containsColumn(String columnName) {
    checkArgument(columnName != null, "Column name cannot be null");
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
  public @Nonnull List<ColumnMetadata> getColumnMetadata() {
    return _columnMetadata;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public @Nonnull String getTextDesc() {
    return _textDesc;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_columnMetadata, _textDesc);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_COLUMN_METADATA, _columnMetadata)
        .add(PROP_TEXT_DESC, _textDesc)
        .toString();
  }

  /** Returns a map from column name to {@link ColumnMetadata} */
  public @Nonnull Map<String, ColumnMetadata> toColumnMap() {
    return toColumnMap(_columnMetadata);
  }

  /** Returns a map from column name to {@link ColumnMetadata} */
  public static @Nonnull Map<String, ColumnMetadata> toColumnMap(
      List<ColumnMetadata> columnMetadata) {
    return columnMetadata.stream()
        .collect(ImmutableMap.toImmutableMap(ColumnMetadata::getName, Function.identity()));
  }
}
