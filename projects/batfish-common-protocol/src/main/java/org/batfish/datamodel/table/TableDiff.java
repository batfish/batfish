package org.batfish.datamodel.table;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.Schema.Type;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.Row.RowBuilder;

/** A utility class to diff two tables */
public final class TableDiff {

  private TableDiff() {}

  static final String MISSING_KEY_BASE = "Key missing from base";
  static final String MISSING_KEY_DELTA = "Key missing from delta";
  static final String NULL_VALUE_BASE = "Base value is null";
  static final String NULL_VALUE_DELTA = "Delta value is null";

  public static final String RESULT_DIFFERENT = "<Different>";
  public static final String RESULT_SAME = "<Same>";

  @VisibleForTesting
  static String baseColumnName(String originalColumnName) {
    return "Base_" + originalColumnName;
  }

  @VisibleForTesting
  static String deltaColumnName(String originalColumnName) {
    return "Delta_" + originalColumnName;
  }

  @VisibleForTesting
  static String diffColumnDescription(String originalColumnName) {
    return "Difference between base and delta for " + originalColumnName;
  }

  @VisibleForTesting
  static String diffColumnName(String originalColumnName) {
    return "Diff_" + originalColumnName;
  }

  @VisibleForTesting
  static String resultDifferent(String message) {
    return RESULT_DIFFERENT + ": " + message;
  }

  /**
   * Computes the String representation of the difference for base and delta values.
   *
   * <p>The underlying Schema of both values should be the same, though onr or both values may be
   * null.
   */
  @VisibleForTesting
  static String diffCells(Object baseValue, Object deltaValue, Schema schema) {
    if (baseValue == null && deltaValue == null) {
      return RESULT_SAME;
    } else if (baseValue == null) {
      return resultDifferent(NULL_VALUE_BASE);
    } else if (deltaValue == null) {
      return resultDifferent(NULL_VALUE_DELTA);
    } else if (baseValue.equals(deltaValue)) {
      return RESULT_SAME;
    }
    if (schema.equals(Schema.INTEGER)) {
      return resultDifferent(Objects.toString((Integer) baseValue - (Integer) deltaValue));
    } else if (schema.getType() == Type.SET) {
      Sets.SetView<?> added = Sets.difference((Set<?>) baseValue, (Set<?>) deltaValue);
      Sets.SetView<?> removed = Sets.difference((Set<?>) deltaValue, (Set<?>) baseValue);
      StringBuilder diff = new StringBuilder();
      if (!added.isEmpty()) {
        diff.append("\n + " + Objects.toString(added.immutableCopy()));
      }
      if (!removed.isEmpty()) {
        diff.append("\n - " + Objects.toString(removed.immutableCopy()));
      }
      return resultDifferent(diff.toString());
    } else {
      return RESULT_DIFFERENT;
    }
  }

  /**
   * Computes the metadata for the "diff" table given the metadata of the original table(s).
   *
   * <p>Treats columns that are keys in input tables as keys of the diff table. For each column in
   * the input tables that is a value (but not a key), three columns are included, two for base and
   * delta values and one for the difference of the two. Columns that are neither keys nor values
   * are ignored.
   */
  @VisibleForTesting
  static TableMetadata diffMetadata(TableMetadata inputMetadata) {
    Builder<ColumnMetadata> diffColumnMetatadata = new Builder<ColumnMetadata>();
    // We two do passes so we can insert all keys first
    for (ColumnMetadata cm : inputMetadata.getColumnMetadata()) {
      if (cm.getIsKey()) {
        diffColumnMetatadata.add(
            new ColumnMetadata(cm.getName(), cm.getSchema(), cm.getDescription(), true, false));
      }
    }
    String dhintText =
        "["
            + String.join(
                ", ",
                diffColumnMetatadata
                    .build()
                    .stream()
                    .map(cm -> cm.getName())
                    .collect(Collectors.toList()))
            + "]";
    for (ColumnMetadata cm : inputMetadata.getColumnMetadata()) {
      if (!cm.getIsKey() && cm.getIsValue()) {
        diffColumnMetatadata.add(
            new ColumnMetadata(
                diffColumnName(cm.getName()),
                Schema.STRING,
                diffColumnDescription(cm.getName()),
                false,
                true),
            new ColumnMetadata(
                baseColumnName(cm.getName()), cm.getSchema(), cm.getDescription(), false, false),
            new ColumnMetadata(
                deltaColumnName(cm.getName()), cm.getSchema(), cm.getDescription(), false, false));
      }
    }

    return new TableMetadata(diffColumnMetatadata.build(), new DisplayHints(null, null, dhintText));
  }

  /**
   * Computes the difference between two rows with respect to {@code columns}.
   *
   * <p>For each column, three columns are generated, consistent with {@link #diffMetadata}. These
   * columns are inserted into the provided {@code rowBuilder}.
   *
   * @throws IllegalArgumentException If both rows are null, rowBuilder is null, or columns is null
   */
  @VisibleForTesting
  static void diffRowValues(
      RowBuilder rowBuilder, Row baseRow, Row deltaRow, List<ColumnMetadata> columns) {
    checkArgument(baseRow != null || deltaRow != null, "Both base and delta rows cannot be null");
    checkArgument(rowBuilder != null, "rowBuilder cannot be null");
    checkArgument(columns != null, "columns cannot be null");

    for (ColumnMetadata cm : columns) {
      String cname = cm.getName();
      if (baseRow == null) {
        rowBuilder
            .put(diffColumnName(cname), resultDifferent(MISSING_KEY_BASE))
            .put(baseColumnName(cname), null)
            .put(deltaColumnName(cname), deltaRow.get(cname));
      } else if (deltaRow == null) {
        rowBuilder
            .put(diffColumnName(cname), resultDifferent(MISSING_KEY_DELTA))
            .put(baseColumnName(cname), baseRow.get(cname))
            .put(deltaColumnName(cname), null);
      } else {
        Object baseValue = baseRow.get(cname, cm.getSchema());
        Object deltaValue = deltaRow.get(cname, cm.getSchema());
        rowBuilder
            .put(diffColumnName(cname), diffCells(baseValue, deltaValue, cm.getSchema()))
            .put(baseColumnName(cname), baseValue)
            .put(deltaColumnName(cname), deltaValue);
      }
    }
  }

  /**
   * Computes the difference table of the two tables
   *
   * @throws IllegalArgumentException if the input column metadatas are not equal.
   */
  public static TableAnswerElement diffTables(
      TableAnswerElement baseTable, TableAnswerElement deltaTable) {
    checkArgument(
        baseTable
            .getMetadata()
            .getColumnMetadata()
            .equals(deltaTable.getMetadata().getColumnMetadata()),
        "Cannot diff tables with different column metadatas");

    TableMetadata inputMetadata = baseTable.getMetadata();
    TableAnswerElement diffTable = new TableAnswerElement(diffMetadata(inputMetadata));

    List<String> keyColumns =
        inputMetadata
            .getColumnMetadata()
            .stream()
            .filter(cm -> cm.getIsKey())
            .map(cm -> cm.getName())
            .collect(Collectors.toList());

    List<ColumnMetadata> valueColumns =
        inputMetadata
            .getColumnMetadata()
            .stream()
            .filter(cm -> (!cm.getIsKey() && cm.getIsValue()))
            .collect(Collectors.toList());

    Set<Object> processedKeys = new HashSet<>();

    Iterator<Row> baseRows = baseTable.getRows().iterator();
    while (baseRows.hasNext()) {
      Row baseRow = baseRows.next();
      Object baseKey = baseRow.getKey(inputMetadata.getColumnMetadata());
      if (processedKeys.contains(baseKey)) {
        continue;
      }
      processedKeys.add(baseKey);
      Row deltaRow = deltaTable.getRows().getRow(baseKey, inputMetadata.getColumnMetadata());
      if (deltaRow != null) {
        Object baseValues = baseRow.getValue(valueColumns);
        Object deltaValues = deltaRow.getValue(valueColumns);
        if (baseValues.equals(deltaValues)) { // skip rows with identical values
          continue;
        }
      }
      RowBuilder diffRowBuilder = Row.builder(baseRow, keyColumns);
      diffRowValues(diffRowBuilder, baseRow, deltaRow, valueColumns);
      diffTable.addRow(diffRowBuilder.build());
    }
    // process keys that are present only in delta
    Iterator<Row> deltaRows = deltaTable.getRows().iterator();
    while (deltaRows.hasNext()) {
      Row deltaRow = deltaRows.next();
      Object deltaKey = deltaRow.getKey(inputMetadata.getColumnMetadata());
      if (processedKeys.contains(deltaKey)) {
        continue;
      }
      processedKeys.add(deltaKey);
      RowBuilder diffRowBuilder = Row.builder(deltaRow, keyColumns);
      diffRowValues(diffRowBuilder, null, deltaRow, valueColumns);
      diffTable.addRow(diffRowBuilder.build());
    }

    return diffTable;
  }
}
