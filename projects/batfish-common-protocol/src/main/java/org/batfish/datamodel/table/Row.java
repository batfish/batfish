package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Exclusion;

/**
 * Represents one row of the table answer. Each row is basically a map of key value pairs, where the
 * key is the column name and the value (currently) is JsonNode.
 */
public class Row implements Comparable<Row> {

  public static class RowBuilder {

    private final ObjectNode _data;

    private RowBuilder() {
      _data = BatfishObjectMapper.mapper().createObjectNode();
    }

    private RowBuilder(Row row) {
      this();
      row.getColumnNames().forEach(col -> _data.set(col, row.get(col)));
    }

    public Row build() {
      return new Row(_data);
    }

    /**
     * Sets the value for the specified column to the specified value. Any existing values for the
     * column are overwritten
     *
     * @param columnName The column to set
     * @param value The value to set
     * @return The RowBuilder object itself (to aid chaining)
     */
    public RowBuilder put(String columnName, Object value) {
      _data.set(columnName, BatfishObjectMapper.mapper().valueToTree(value));
      return this;
    }
  }

  private final ObjectNode _data;

  /**
   * Returns a new {@link Row} with the given entries.
   *
   * <p>This function requires an even number of parameters, where the 0th and every even parameter
   * is a {@link String} representing the name of a column.
   */
  public static Row of(Object... objects) {
    checkArgument(
        objects.length % 2 == 0, "expecting an even number of parameters, not %s", objects.length);
    Row.RowBuilder builder = Row.builder();
    for (int i = 0; i + 1 < objects.length; i += 2) {
      checkArgument(
          objects[i] instanceof String, "argument %s must be a string, but is: %s", i, objects[i]);
      builder.put((String) objects[i], objects[i + 1]);
    }
    return builder.build();
  }

  @JsonCreator
  public Row(ObjectNode data) {
    _data = firstNonNull(data, BatfishObjectMapper.mapper().createObjectNode());
  }

  /** Returns a builder object for Row */
  public static RowBuilder builder() {
    return new RowBuilder();
  }

  /** Returns a builder object for Row seeded by the contents of {@code otheRow} */
  public static RowBuilder builder(Row otherRow) {
    return new RowBuilder(otherRow);
  }

  /**
   * Compares two Rows. The current implementation ignores primary keys of the table and compares
   * everything, mainly to provide consistent ordering of answers. This will need to change when we
   * start using the primary keys for something.
   *
   * @param o The other Row to compare against.
   * @return The result of the comparison
   */
  @Override
  public int compareTo(Row o) {
    try {
      String myStr = BatfishObjectMapper.mapper().writeValueAsString(_data);
      String oStr = BatfishObjectMapper.mapper().writeValueAsString(o._data);
      return myStr.compareTo(oStr);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Exception in row comparison", e);
    }
  }

  private <T> T convertType(JsonNode jsonNode, Class<T> valueType) {
    try {
      return BatfishObjectMapper.mapper().treeToValue(jsonNode, valueType);
    } catch (JsonProcessingException e) {
      throw new BatfishException(
          String.format(
              "Could not recover object of type %s from json %s", valueType.getName(), jsonNode),
          e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof Row)) {
      return false;
    }
    return _data.equals(((Row) o)._data);
  }

  /**
   * Gets the (raw) Json representation of the object stored in the row
   *
   * @param columnName The column to fetch
   * @return The {@link JsonNode} object that represents the stored object
   * @throws {@link NoSuchElementException} if this column does not exist
   */
  public JsonNode get(String columnName) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(getMissingColumnErrorMessage(columnName));
    }
    return _data.get(columnName);
  }

  /**
   * Gets the value of specified column name
   *
   * @param columnName The column to fetch
   * @return The result
   * @throws {@link NoSuchElementException} if this column is not present
   */
  public <T> T get(String columnName, Class<T> valueType) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(getMissingColumnErrorMessage(columnName));
    }
    if (_data.get(columnName) == null) {
      return null;
    }
    return convertType(_data.get(columnName), valueType);
  }

  /**
   * Gets the value of specified column name
   *
   * @param columnName The column to fetch
   * @return The result
   * @throws {@link NoSuchElementException} if this column is not present
   */
  public <T> T get(String columnName, TypeReference<T> valueTypeRef) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(getMissingColumnErrorMessage(columnName));
    }
    if (_data.get(columnName) == null) {
      return null;
    }
    try {
      return BatfishObjectMapper.mapper()
          .readValue(
              BatfishObjectMapper.mapper().treeAsTokens(_data.get(columnName)), valueTypeRef);
    } catch (IOException e) {
      throw new BatfishException(
          String.format(
              "Could not recover object of type %s from column %s",
              valueTypeRef.getClass(), columnName),
          e);
    }
  }

  /**
   * Gets the value of specified column
   *
   * @param columnName The column to fetch
   * @return The result
   * @throws {@link NoSuchElementException} if this column is not present
   */
  public Object get(String columnName, Schema columnSchema) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(getMissingColumnErrorMessage(columnName));
    }
    if (_data.get(columnName) == null) {
      return null;
    }
    if (columnSchema.isList()) {
      List<JsonNode> list = get(columnName, new TypeReference<List<JsonNode>>() {});
      return list.stream()
          .map(in -> convertType(in, columnSchema.getBaseType()))
          .collect(Collectors.toList());
    } else {
      return convertType(_data.get(columnName), columnSchema.getBaseType());
    }
  }

  /**
   * Fetch the names of the columns in this Row
   *
   * @return The {@link Set} of names
   */
  public Set<String> getColumnNames() {
    HashSet<String> columns = new HashSet<>();
    _data.fieldNames().forEachRemaining(column -> columns.add(column));
    return columns;
  }

  @JsonValue
  private ObjectNode getData() {
    return _data;
  }

  /**
   * Returns the list of values in all columns declared as key in the metadata.
   *
   * @param metadata Provides information on which columns are key and their {@link Schema}
   * @return The list
   */
  public List<Object> getKey(TableMetadata metadata) {
    List<Object> keyList = new LinkedList<>();
    for (ColumnMetadata column : metadata.getColumnMetadata()) {
      if (column.getIsKey()) {
        keyList.add(get(column.getName(), column.getSchema()));
      }
    }
    return keyList;
  }

  private String getMissingColumnErrorMessage(String columnName) {
    return String.format(
        "Column '%s' is not present. Valid columns are: %s", columnName, getColumnNames());
  }

  /**
   * Returns the list of values in all columns declared as value in the metadata.
   *
   * @param metadata Provides information on which columns are key and their {@link Schema}
   * @return The list
   */
  public List<Object> getValue(TableMetadata metadata) {
    List<Object> valueList = new LinkedList<>();
    for (ColumnMetadata column : metadata.getColumnMetadata()) {
      if (column.getIsValue()) {
        valueList.add(get(column.getName(), column.getSchema()));
      }
    }
    return valueList;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_data);
  }

  /**
   * Checks is this row is covered by the provided exclusion.
   *
   * @param exclusion The exclusion to check against.
   * @return The result of the check
   */
  public boolean isCovered(ObjectNode exclusion) {
    return Exclusion.firstCoversSecond(exclusion, _data);
  }

  /**
   * Returns a new {@link Row} that has only the specified columns from this row.
   *
   * @param columns The columns to keep.
   * @return A new {@link Row} object
   * @throws {@link NoSuchElementException} if one of the specified columns are not present
   */
  public static Row selectColumns(Row inputRow, Set<String> columns) {
    RowBuilder retRow = Row.builder();
    columns.forEach(col -> retRow.put(col, inputRow.get(col)));
    return retRow.build();
  }

  @Override
  public String toString() {
    return _data.toString();
  }
}
