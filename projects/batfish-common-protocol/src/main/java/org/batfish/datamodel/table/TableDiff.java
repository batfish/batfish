package org.batfish.datamodel.table;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.Schema.Type;
import org.batfish.datamodel.table.Row.RowBuilder;

/** A utility class to diff two tables */
@ParametersAreNonnullByDefault
public final class TableDiff {

  private TableDiff() {}

  private static final String COL_BASE_PREFIX = "Base_";
  private static final String COL_DELTA_PREFIX = "Delta_";
  private static final String COL_DIFF_PREFIX = "Diff_";

  public static final String COL_KEY_PRESENCE = "KeyPresence";
  public static final String COL_KEY_PRESENCE_DESC = "In which table(s) is the key present";
  public static final String COL_KEY_STATUS_BOTH = "In both";
  public static final String COL_KEY_STATUS_ONLY_BASE = "Only in Base";
  public static final String COL_KEY_STATUS_ONLY_DELTA = "Only in Delta";

  static final String NULL_VALUE_BASE = "Base value is null";
  private static final String NULL_VALUE_DELTA = "Delta value is null";

  public static final String RESULT_DIFFERENT = "<Different>";
  public static final String RESULT_SAME = "<Same>";

  /** Returns the modified column name to represent the delta value of the original column */
  public static String baseColumnName(String originalColumnName) {
    return COL_BASE_PREFIX + originalColumnName;
  }

  /**
   * Returns a map over {@code rows}, where the key is the key of the {@link Row} and the value is a
   * list of {@link Row}s with that key.
   */
  @VisibleForTesting
  static Map<List<Object>, List<Row>> buildMap(Rows rows, List<ColumnMetadata> metadata) {
    Map<List<Object>, List<Row>> map = new HashMap<>();
    Iterator<Row> iterator = rows.iterator();
    while (iterator.hasNext()) {
      Row row = iterator.next();
      map.computeIfAbsent(row.getKey(metadata), k -> new LinkedList<>()).add(row);
    }
    return map;
  }

  /** Returns the modified column name to represent the delta value of the original column */
  public static String deltaColumnName(String originalColumnName) {
    return COL_DELTA_PREFIX + originalColumnName;
  }

  @VisibleForTesting
  static String diffColumnDescription(String originalColumnName) {
    return "Difference between base and delta for " + originalColumnName;
  }

  /** Returns the modified column name to represent the delta value of the original column */
  public static String diffColumnName(String originalColumnName) {
    return COL_DIFF_PREFIX + originalColumnName;
  }

  @VisibleForTesting
  static String resultDifferent(String message) {
    return RESULT_DIFFERENT + ": " + message;
  }

  /**
   * Computes the String representation of the difference for base and delta values.
   *
   * <p>The underlying Schema of both values should be the same, though one or both values may be
   * null. Meaningful diffs are currently computed only for Integers and Sets.
   */
  @VisibleForTesting
  static String diffCells(@Nullable Object baseValue, @Nullable Object deltaValue, Schema schema) {
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
        diff.append("\n + ").append(Objects.toString(added.immutableCopy()));
      }
      if (!removed.isEmpty()) {
        diff.append("\n - ").append(Objects.toString(removed.immutableCopy()));
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
   * delta values and one for the difference of the two. For remaining columns (which are neither
   * keys nor values), two columns are included for base and delta values.
   */
  @VisibleForTesting
  static TableMetadata diffMetadata(TableMetadata inputMetadata) {
    ImmutableList.Builder<ColumnMetadata> diffColumnMetatadata = ImmutableList.builder();
    // 1. Insert all key columns
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
                    .map(ColumnMetadata::getName)
                    .collect(Collectors.toList()))
            + "]";

    // 2. Insert the key status column
    diffColumnMetatadata.add(
        new ColumnMetadata(COL_KEY_PRESENCE, Schema.STRING, COL_KEY_PRESENCE_DESC, false, true));

    // 3. Add other columns
    for (ColumnMetadata cm : inputMetadata.getColumnMetadata()) {
      if (cm.getIsKey()) {
        continue;
      }
      if (cm.getIsValue()) {
        diffColumnMetatadata.add(
            new ColumnMetadata(
                diffColumnName(cm.getName()),
                Schema.STRING,
                diffColumnDescription(cm.getName()),
                false,
                true));
      }
      diffColumnMetatadata.add(
          new ColumnMetadata(
              baseColumnName(cm.getName()), cm.getSchema(), cm.getDescription(), false, false),
          new ColumnMetadata(
              deltaColumnName(cm.getName()), cm.getSchema(), cm.getDescription(), false, false));
    }

    return new TableMetadata(diffColumnMetatadata.build(), dhintText);
  }

  /**
   * Computes the difference between two rows with respect to {@code columns}.
   *
   * <p>For each column that is not a key, either two or three columns are generated, depending on
   * whether the column is a value (see {@link #diffMetadata}. These columns are inserted into the
   * provided {@code rowBuilder}.
   *
   * @throws IllegalArgumentException If both rows are null, rowBuilder is null, or columns is null
   */
  @VisibleForTesting
  static void diffRowValues(
      RowBuilder rowBuilder,
      @Nullable Row baseRow,
      @Nullable Row deltaRow,
      TableMetadata inputMetadata) {
    checkArgument(baseRow != null || deltaRow != null, "Both base and delta rows cannot be null");
    checkArgument(rowBuilder != null, "rowBuilder cannot be null");
    checkArgument(inputMetadata.getColumnMetadata() != null, "columns cannot be null");

    String keyStatus =
        baseRow == null
            ? COL_KEY_STATUS_ONLY_DELTA
            : deltaRow == null ? COL_KEY_STATUS_ONLY_BASE : COL_KEY_STATUS_BOTH;
    rowBuilder.put(COL_KEY_PRESENCE, keyStatus);

    for (ColumnMetadata cm : inputMetadata.getColumnMetadata()) {
      if (cm.getIsKey()) {
        continue;
      }
      Object baseValue = baseRow == null ? null : baseRow.get(cm.getName(), cm.getSchema());
      Object deltaValue = deltaRow == null ? null : deltaRow.get(cm.getName(), cm.getSchema());
      if (cm.getIsValue()) {
        if (baseRow == null || deltaRow == null) {
          rowBuilder.put(diffColumnName(cm.getName()), resultDifferent(keyStatus));
        } else {
          rowBuilder.put(
              diffColumnName(cm.getName()), diffCells(baseValue, deltaValue, cm.getSchema()));
        }
      }
      rowBuilder
          .put(baseColumnName(cm.getName()), baseValue)
          .put(deltaColumnName(cm.getName()), deltaValue);
    }
  }

  /**
   * Computes the difference table of the two tables by taking a "join" on the key columns. If
   * includeOneTableKeys is true, then an "inner join" is done, that is, only keys that are present
   * in both tables are in the output. Otherwise, a "full outer join" is done and any key that is in
   * either table makes it to the output.
   *
   * @throws IllegalArgumentException if the input column metadatas are not equal.
   */
  public static TableAnswerElement diffTables(
      TableAnswerElement baseTable, TableAnswerElement deltaTable, boolean includeOneTableKeys) {
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
            .filter(ColumnMetadata::getIsKey)
            .map(ColumnMetadata::getName)
            .collect(Collectors.toList());

    List<ColumnMetadata> valueColumns =
        inputMetadata
            .getColumnMetadata()
            .stream()
            .filter(cm -> (!cm.getIsKey() && cm.getIsValue()))
            .collect(Collectors.toList());

    Set<Object> baseKeys = new HashSet<>();

    Iterator<Row> baseRows = baseTable.getRows().iterator();
    Map<List<Object>, List<Row>> deltaMap =
        buildMap(deltaTable.getRows(), inputMetadata.getColumnMetadata());
    while (baseRows.hasNext()) {
      Row baseRow = baseRows.next();
      Object baseKey = baseRow.getKey(inputMetadata.getColumnMetadata());
      baseKeys.add(baseKey);
      List<Row> deltaRows = deltaMap.get(baseKey);
      if (deltaRows == null) { // no matching keys in delta table
        if (includeOneTableKeys) {
          RowBuilder diffRowBuilder = Row.builder().putAll(baseRow, keyColumns);
          diffRowValues(diffRowBuilder, baseRow, null, inputMetadata);
          diffTable.addRow(diffRowBuilder.build());
        }
      } else {
        for (Row deltaRow : deltaRows) {
          // insert delta rows that are unequal
          if (!baseRow.getValue(valueColumns).equals(deltaRow.getValue(valueColumns))) {
            RowBuilder diffRowBuilder = Row.builder().putAll(baseRow, keyColumns);
            diffRowValues(diffRowBuilder, baseRow, deltaRow, inputMetadata);
            diffTable.addRow(diffRowBuilder.build());
          }
        }
      }
    }
    if (includeOneTableKeys) {
      // process keys that are present only in delta
      Iterator<Row> deltaRows = deltaTable.getRows().iterator();
      while (deltaRows.hasNext()) {
        Row deltaRow = deltaRows.next();
        Object deltaKey = deltaRow.getKey(inputMetadata.getColumnMetadata());
        if (baseKeys.contains(deltaKey)) {
          continue;
        }
        RowBuilder diffRowBuilder = Row.builder().putAll(deltaRow, keyColumns);
        diffRowValues(diffRowBuilder, null, deltaRow, inputMetadata);
        diffTable.addRow(diffRowBuilder.build());
      }
    }

    return diffTable;
  }
}
