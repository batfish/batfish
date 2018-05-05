package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
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

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof Row)) {
      return false;
    }
    return _data.equals(((Row) o)._data);
  }

  /**
   * Gets the value of specified column name
   *
   * @param columnName The column to fetch
   * @return The result
   */
  public <T> T get(String columnName, Class<T> valueType) {
    try {
      return BatfishObjectMapper.mapper().treeToValue(_data.get(columnName), valueType);
    } catch (JsonProcessingException e) {
      throw new BatfishException(
          String.format(
              "Could not recover object of type %s from column %s",
              valueType.getName(), columnName),
          e);
    }
  }

  public <T> T get(String columnName, TypeReference<?> valueTypeRef) {
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

  @JsonValue
  private ObjectNode getData() {
    return _data;
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
}
