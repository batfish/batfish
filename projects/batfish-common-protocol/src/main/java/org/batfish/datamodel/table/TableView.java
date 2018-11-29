package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.datamodel.answers.AnswerElement;

@ParametersAreNonnullByDefault
public final class TableView extends AnswerElement {

  private static final String PROP_OPTIONS = "options";

  private static final String PROP_ROWS = "rows";

  private static final String PROP_TABLE_METADATA = "metadata";

  @JsonCreator
  private static @Nonnull TableView create(
      @JsonProperty(PROP_OPTIONS) @Nullable AnswerRowsOptions options,
      @JsonProperty(PROP_ROWS) @Nullable List<TableViewRow> rows,
      @JsonProperty(PROP_TABLE_METADATA) @Nullable TableMetadata tableMetadata) {
    checkArgument(options != null, "Missing %s", PROP_OPTIONS);
    checkArgument(tableMetadata != null, "Missing %s", PROP_TABLE_METADATA);
    return new TableView(
        options, ImmutableList.copyOf(firstNonNull(rows, ImmutableList.of())), tableMetadata);
  }

  private final AnswerRowsOptions _options;

  private final List<TableViewRow> _rows;

  private final TableMetadata _tableMetadata;

  public TableView(
      AnswerRowsOptions options, List<TableViewRow> rows, TableMetadata tableMetadata) {
    _options = options;
    _rows = rows;
    _tableMetadata = tableMetadata;
  }

  @JsonProperty(PROP_OPTIONS)
  public @Nonnull AnswerRowsOptions getOptions() {
    return _options;
  }

  @JsonProperty(PROP_ROWS)
  public @Nonnull List<TableViewRow> getRows() {
    return _rows;
  }

  @JsonProperty(PROP_TABLE_METADATA)
  public @Nonnull TableMetadata getTableMetadata() {
    return _tableMetadata;
  }

  @JsonIgnore
  public List<Row> getInnerRows() {
    return _rows.stream().map(TableViewRow::getRow).collect(ImmutableList.toImmutableList());
  }
}
