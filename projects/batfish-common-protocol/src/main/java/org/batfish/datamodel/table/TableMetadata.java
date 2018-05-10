package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.DisplayHints;

public class TableMetadata {

  private static final String PROP_COLUMN_METADATA = "columnMetadata";

  private static final String PROP_DISPLAY_HINTS = "displayHints";

  @Nonnull private SortedMap<String, ColumnMetadata> _columnMetadata;

  @Nullable private DisplayHints _displayHints;

  public TableMetadata() {
    this(null, null);
  }

  @JsonCreator
  public TableMetadata(
      @Nullable @JsonProperty(PROP_COLUMN_METADATA) Map<String, ColumnMetadata> columnData,
      @Nullable @JsonProperty(PROP_DISPLAY_HINTS) DisplayHints displayHints) {
    _columnMetadata = ImmutableSortedMap.copyOf(firstNonNull(columnData, new TreeMap<>()));
    _displayHints = displayHints;
  }

  @JsonProperty(PROP_COLUMN_METADATA)
  public Map<String, ColumnMetadata> getColumnMetadata() {
    return _columnMetadata;
  }

  @JsonProperty(PROP_DISPLAY_HINTS)
  public DisplayHints getTextDesc() {
    return _displayHints;
  }
}
