package org.batfish.datamodel.answers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.ExcludedRows;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;

public final class AnswerMetadataUtil {

  public static @Nonnull AnswerMetadata computeAnswerMetadata(
      @Nonnull Answer answer, @Nonnull BatfishLogger logger) {
    try {
      return AnswerMetadata.builder()
          .setMetrics(computeMetrics(answer, logger))
          .setStatus(answer.getStatus())
          .build();
    } catch (Exception e) {
      return AnswerMetadata.forStatus(AnswerStatus.FAILURE);
    }
  }

  @VisibleForTesting
  static @Nullable ColumnAggregationResult computeColumnAggregation(
      @Nonnull TableAnswerElement table,
      @Nonnull ColumnAggregation columnAggregation,
      @Nonnull BatfishLogger logger) {
    String column = columnAggregation.getColumn();
    Aggregation aggregation = columnAggregation.getAggregation();
    Object value =
        switch (aggregation) {
          case MAX -> computeColumnMax(table, column, logger);
        };
    return value == null ? null : new ColumnAggregationResult(aggregation, column, value);
  }

  @VisibleForTesting
  static @Nonnull Map<String, Map<Aggregation, Object>> computeColumnAggregations(
      @Nonnull TableAnswerElement table,
      @Nonnull List<ColumnAggregation> aggregations,
      @Nonnull BatfishLogger logger) {
    Map<String, Map<Aggregation, Object>> columnAggregations = new HashMap<>();
    aggregations.stream()
        .map(columnAggregation -> computeColumnAggregation(table, columnAggregation, logger))
        .filter(Objects::nonNull)
        .forEach(
            columnAggregationResult ->
                columnAggregations
                    .computeIfAbsent(
                        columnAggregationResult.getColumn(), c -> new EnumMap<>(Aggregation.class))
                    .computeIfAbsent(
                        columnAggregationResult.getAggregation(),
                        a -> columnAggregationResult.getValue()));
    return CollectionUtil.toImmutableMap(
        columnAggregations,
        Entry::getKey,
        columnAggregationsByColumnEntry ->
            CollectionUtil.toImmutableMap(
                columnAggregationsByColumnEntry.getValue(), Entry::getKey, Entry::getValue));
  }

  /**
   * Return the largest non-null value in the specified column. If the column cannot be interpreted
   * as an Integer,or has only null values, returns null.
   *
   * @throws IllegalArgumentException if the named column is not present in the table.
   */
  @VisibleForTesting
  static @Nullable Integer computeColumnMax(
      @Nonnull TableAnswerElement table, @Nonnull String column, @Nonnull BatfishLogger logger) {
    ColumnMetadata columnMetadata = table.getMetadata().toColumnMap().get(column);
    if (columnMetadata == null) {
      String message = String.format("No column named: %s", column);
      logger.errorf("%s\n", message);
      throw new IllegalArgumentException(message);
    }
    Schema schema = columnMetadata.getSchema();
    Function<Row, Integer> rowToInteger;
    if (schema.equals(Schema.INTEGER)) {
      rowToInteger = r -> r.getInteger(column);
    } else if (schema.equals(Schema.ISSUE)) {
      rowToInteger = r -> r.getIssue(column).getSeverity();
    } else {
      // unsupported
      return null;
    }
    return table.getRows().getData().stream()
        .map(rowToInteger)
        .filter(Objects::nonNull)
        .max(Comparator.naturalOrder())
        .orElse(null);
  }

  @VisibleForTesting
  static Set<String> computeEmptyColumns(TableAnswerElement table) {
    return table.getMetadata().toColumnMap().keySet().stream()
        .filter(column -> table.getRowsList().stream().allMatch(row -> !row.hasNonNull(column)))
        .collect(ImmutableSet.toImmutableSet());
  }

  @VisibleForTesting
  static @Nullable Metrics computeMetrics(@Nonnull Answer answer, @Nonnull BatfishLogger logger) {
    if (answer.getAnswerElements().isEmpty()) {
      return null;
    }
    AnswerElement ae = answer.getAnswerElements().get(0);
    if (!(ae instanceof TableAnswerElement)) {
      return null;
    }
    TableAnswerElement table = (TableAnswerElement) ae;
    int numRows = table.getRowsList().size();
    int numExcludedRows =
        table.getExcludedRows().stream().map(ExcludedRows::getRowsList).mapToInt(List::size).sum();
    ImmutableList.Builder<ColumnAggregation> columnAggregationsBuilder = ImmutableList.builder();
    table.getMetadata().getColumnMetadata().stream()
        .map(ColumnMetadata::getName)
        .forEach(
            column ->
                Arrays.stream(Aggregation.values())
                    .forEach(
                        aggregation ->
                            columnAggregationsBuilder.add(
                                new ColumnAggregation(aggregation, column))));
    Map<String, Map<Aggregation, Object>> columnAggregationResults =
        computeColumnAggregations(table, columnAggregationsBuilder.build(), logger);
    Set<String> emptyColumns = computeEmptyColumns(table);
    return Metrics.builder()
        .setAggregations(columnAggregationResults)
        .setEmptyColumns(emptyColumns)
        .setNumExcludedRows(numExcludedRows)
        .setNumRows(numRows)
        .build();
  }

  private AnswerMetadataUtil() {}
}
