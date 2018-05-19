package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;

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

  private final ObjectNode _data;

  public Row() {
    this(null);
  }

  @JsonCreator
  public Row(ObjectNode data) {
    _data = firstNonNull(data, BatfishObjectMapper.mapper().createObjectNode());
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

  private static <T> T convertType(JsonNode jsonNode, Class<T> valueType) {
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
   */
  public JsonNode get(String columnName) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(getMissingColumnErrorMessage(columnName));
    }
    return _data.get(columnName);
  }

  private String getMissingColumnErrorMessage(String columnName) {
    return String.format(
        "Column '%s' is not present. Valid columns are: %s", columnName, getColumnNames());
  }

  /**
   * Gets the value of specified column name
   *
   * @param columnName The column to fetch
   * @return The result
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
   */
  public <T> T get(String columnName, TypeReference<?> valueTypeRef) {
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
   * Sets the value for the specified column to the specified value. Any existing values for the
   * column are overwritten
   *
   * @param columnName The column to set
   * @param value The value to set
   * @return The Row object itself (to aid chaining)
   */
  public Row put(String columnName, Object value) {
    _data.set(columnName, BatfishObjectMapper.mapper().valueToTree(value));
    return this;
  }

  /**
   * Removes the specified column from this row
   *
   * @param columnName The column to remove
   * @return The Row object itself (to aid chaining)
   */
  public Row remove(String columnName) {
    _data.remove(columnName);
    return this;
  }

  @Override
  public String toString() {
    return _data.toString();
  }
}
