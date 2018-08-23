package org.batfish.datamodel.answers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;

public final class AnswerMetadataUtil {

  private AnswerMetadataUtil() {}

  public static @Nonnull AnswerMetadata computeAnswerMetadata(
      @Nonnull Answer answer,
      @Nonnull List<ColumnAggregation> columnAggregations,
      BatfishLogger logger) {
    try {
      return new AnswerMetadata(
          computeMetrics(answer, columnAggregations, logger), answer.getStatus());
    } catch (Exception e) {
      return new AnswerMetadata(null, AnswerStatus.FAILURE);
    }
  }

  @VisibleForTesting
  static @Nullable Metrics computeMetrics(
      Answer answer, List<ColumnAggregation> columnAggregations, BatfishLogger logger) {
    if (answer.getAnswerElements().isEmpty()) {
      return null;
    }
    AnswerElement ae = answer.getAnswerElements().get(0);
    if (!(ae instanceof TableAnswerElement)) {
      return null;
    }
    TableAnswerElement table = (TableAnswerElement) ae;
    int numRows = table.getRowsList().size();
    List<ColumnAggregationResult> columnAggregationResults =
        computeColumnAggregations(table, columnAggregations, logger);
    return new Metrics(columnAggregationResults, numRows);
  }

  @VisibleForTesting
  @Nonnull
  static List<ColumnAggregationResult> computeColumnAggregations(
      @Nonnull TableAnswerElement table,
      @Nonnull List<ColumnAggregation> aggregations,
      BatfishLogger logger) {
    return aggregations
        .stream()
        .map(columnAggregation -> computeColumnAggregation(table, columnAggregation, logger))
        .collect(ImmutableList.toImmutableList());
  }

  @VisibleForTesting
  @Nonnull
  static ColumnAggregationResult computeColumnAggregation(
      @Nonnull TableAnswerElement table,
      ColumnAggregation columnAggregation,
      BatfishLogger logger) {
    Object value;
    String column = columnAggregation.getColumn();
    Aggregation aggregation = columnAggregation.getAggregation();
    switch (aggregation) {
      case MAX:
        value = computeColumnMax(table, column, logger);
        break;
      default:
        logger.errorf("Unhandled aggregation type: %s\n", aggregation);
        value = null;
        break;
    }
    return new ColumnAggregationResult(aggregation, column, value);
  }

  @VisibleForTesting
  @Nullable
  static Integer computeColumnMax(
      @Nonnull TableAnswerElement table, @Nonnull String column, BatfishLogger logger) {
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
      String message = String.format("Unsupported schema for MAX aggregation: %s", schema);
      logger.errorf("%s\n", message);
      throw new UnsupportedOperationException(message);
    }
    return table
        .getRows()
        .getData()
        .stream()
        .map(rowToInteger)
        .max(Comparator.naturalOrder())
        .orElse(null);
  }
}
