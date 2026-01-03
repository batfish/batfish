package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SchemaUtils;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Represents one row of the table answer. Each row is basically a map of key value pairs, where the
 * key is the column name and the value (currently) is JsonNode.
 */
@ParametersAreNonnullByDefault
public class Row implements Serializable {

  public abstract static class RowBuilder {

    protected final @Nonnull ObjectNode _data;

    private RowBuilder() {
      _data = BatfishObjectMapper.mapper().createObjectNode();
    }

    public Row build() {
      return new Row(_data.deepCopy());
    }

    @VisibleForTesting
    Row rowOf(Object... objects) {
      checkArgument(
          objects.length % 2 == 0,
          "expecting an even number of parameters, not %s",
          objects.length);
      for (int i = 0; i + 1 < objects.length; i += 2) {
        checkArgument(
            objects[i] instanceof String,
            "argument %s must be a string, but is: %s",
            i,
            objects[i]);
        put((String) objects[i], objects[i + 1]);
      }
      return build();
    }

    /**
     * Sets the value of {@code column} to {@code value}.
     *
     * <p>Any existing values for the column are overwritten
     */
    public RowBuilder put(String column, @Nullable Object value) {
      _data.set(column, BatfishObjectMapper.mapper().valueToTree(value));
      return this;
    }

    /** Mirrors the values of all columns in {@code otherRow} */
    public RowBuilder putAll(Row otherRow) {
      return putAll(otherRow, otherRow.getColumnNames());
    }

    /**
     * Mirrors the values of {@code columns} in {@code otherRow}
     *
     * @throws NoSuchElementException if one of the columns is not present in {@code otherRow}.
     */
    public RowBuilder putAll(Row otherRow, Collection<String> columns) {
      columns.forEach(col -> put(col, otherRow.get(col)));
      return this;
    }
  }

  public static final class TypedRowBuilder extends RowBuilder {

    private final Map<String, ColumnMetadata> _columns;
    private final Set<String> _columnNames;

    private TypedRowBuilder(Map<String, ColumnMetadata> columns) {
      _columns = ImmutableMap.copyOf(columns);
      _columnNames = ImmutableSortedSet.copyOf(columns.keySet());
    }

    /**
     * Puts {@code object} into column {@code column} of the row, after checking if the object is
     * compatible with the Schema of the column
     */
    @Override
    public TypedRowBuilder put(String column, @Nullable Object object) {
      checkArgument(
          _columnNames.contains(column),
          "Column '%s' is not present. Valid columns are: %s",
          column,
          _columnNames);
      Schema expectedSchema = _columns.get(column).getSchema();
      checkArgument(
          SchemaUtils.isValidObject(object, expectedSchema),
          "Cannot convert '%s' to Schema '%s' of column '%s'",
          object,
          expectedSchema,
          column);
      super.put(column, object);
      return this;
    }

    @Override
    public Row build() {
      // Fill in missing columns with null entries
      _columnNames.stream().filter(c -> !_data.has(c)).forEach(c -> super.put(c, null));
      return super.build();
    }
  }

  public static class UntypedRowBuilder extends RowBuilder {
    private UntypedRowBuilder() {}
  }

  private final @Nonnull ObjectNode _data;

  /**
   * Returns a new {@link Row} with the given entries.
   *
   * <p>{@code objects} should be an even number of parameters, where the 0th and every even
   * parameter is a {@link String} representing the name of a column.
   */
  public static Row of(Object... objects) {
    return builder().rowOf(objects);
  }

  /**
   * Returns a new {@link Row} with the given entries.
   *
   * <p>{@code objects} should be an even number of parameters, where the 0th and every even
   * parameter is a {@link String} representing the name of a column. The columns names and the
   * actual objects (in odd parameters) must be compliant with the metadata map in {@code columns}.
   */
  public static Row of(Map<String, ColumnMetadata> columns, Object... objects) {
    return builder(columns).rowOf(objects);
  }

  @JsonCreator
  private Row(ObjectNode data) {
    _data = firstNonNull(data, BatfishObjectMapper.mapper().createObjectNode());
  }

  /** Returns an {@link UntypedRowBuilder} object for Row */
  public static UntypedRowBuilder builder() {
    return new UntypedRowBuilder();
  }

  /** Returns a {@link TypedRowBuilder} object for Row */
  public static TypedRowBuilder builder(Map<String, ColumnMetadata> columns) {
    return new TypedRowBuilder(columns);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Row)) {
      return false;
    }
    return _data.equals(((Row) o)._data);
  }

  /**
   * Gets the (raw) Json representation of the object stored in the row
   *
   * @param columnName The column to fetch
   * @return The {@link JsonNode} object that represents the stored object
   * @throws NoSuchElementException if this column does not exist
   */
  public JsonNode get(String columnName) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(missingColumnErrorMessage(columnName, getColumnNames()));
    }
    return _data.get(columnName);
  }

  /**
   * Gets the value of specified column
   *
   * @param columnName The column to fetch
   * @return The result
   * @throws NoSuchElementException if this column is not present
   * @throws ClassCastException if the recovered data cannot be cast to the expected object
   */
  public Object get(String columnName, Schema columnSchema) {
    if (!_data.has(columnName)) {
      throw new NoSuchElementException(missingColumnErrorMessage(columnName, getColumnNames()));
    }
    if (_data.get(columnName).isNull()) {
      return null;
    }
    return SchemaUtils.convertType(_data.get(columnName), columnSchema);
  }

  /** Get the value of specified column safely cast to type specifed via {@code typeReference}. */
  public <T> T get(String columnName, TypeReference<T> typeReference) {
    ObjectMapper mapper = BatfishObjectMapper.mapper();
    JsonNode node = get(columnName);
    try {
      return mapper.readValue(mapper.treeAsTokens(node), typeReference);
    } catch (IOException e) {
      throw new ClassCastException(
          String.format(
              "Cannot cast row element in column '%s' given the provided TypeReference: %s",
              columnName, Throwables.getStackTraceAsString(e)));
    }
  }

  public Boolean getBoolean(String column) {
    return (Boolean) get(column, Schema.BOOLEAN);
  }

  /**
   * Fetch the names of the columns in this Row
   *
   * @return The {@link Set} of names
   */
  public Set<String> getColumnNames() {
    HashSet<String> columns = new HashSet<>();
    _data.fieldNames().forEachRemaining(columns::add);
    return columns;
  }

  @JsonValue
  private ObjectNode getData() {
    return _data;
  }

  public Double getDouble(String column) {
    return (Double) get(column, Schema.DOUBLE);
  }

  public FileLines getFileLines(String column) {
    return (FileLines) get(column, Schema.FILE_LINES);
  }

  public Flow getFlow(String column) {
    return (Flow) get(column, Schema.FLOW);
  }

  public Integer getInteger(String column) {
    return (Integer) get(column, Schema.INTEGER);
  }

  /**
   * Returns the list of values in all columns declared as key in the metadata.
   *
   * @param metadata Provides information on which columns are key and their {@link Schema}
   * @return The list
   */
  public List<Object> getKey(List<ColumnMetadata> metadata) {
    List<Object> keyList = new LinkedList<>();
    for (ColumnMetadata column : metadata) {
      if (column.getIsKey()) {
        keyList.add(get(column.getName(), column.getSchema()));
      }
    }
    return keyList;
  }

  public NodeInterfacePair getInterface(String column) {
    return (NodeInterfacePair) get(column, Schema.INTERFACE);
  }

  public Ip getIp(String column) {
    return (Ip) get(column, Schema.IP);
  }

  public Issue getIssue(String column) {
    return (Issue) get(column, Schema.ISSUE);
  }

  public @Nullable Long getLong(String column) {
    return (Long) get(column, Schema.LONG);
  }

  public Node getNode(String column) {
    return (Node) get(column, Schema.NODE);
  }

  public Object getObject(String column) {
    return get(column, Schema.OBJECT);
  }

  public Prefix getPrefix(String column) {
    return (Prefix) get(column, Schema.PREFIX);
  }

  public @Nullable NextHop getNextHop(String column) {
    return (NextHop) get(column, Schema.NEXT_HOP);
  }

  public SelfDescribingObject getSelfDescribing(String column) {
    return (SelfDescribingObject) get(column, Schema.SELF_DESCRIBING);
  }

  public String getString(String column) {
    return (String) get(column, Schema.STRING);
  }

  public @Nullable Trace getTrace(String column) {
    return (Trace) get(column, Schema.TRACE);
  }

  /**
   * Returns the list of values in all columns declared as value in the metadata.
   *
   * @param metadata Provides information on which columns are key and their {@link Schema}
   * @return The list
   */
  public List<Object> getValue(List<ColumnMetadata> metadata) {
    List<Object> valueList = new LinkedList<>();
    for (ColumnMetadata column : metadata) {
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

  /** Returns a message indicating that {@code columnName} is not present in {@code columns} */
  public static String missingColumnErrorMessage(String columnName, Set<String> columns) {
    return String.format("Column '%s' is not present. Valid columns are: %s", columnName, columns);
  }

  @Override
  public String toString() {
    return _data.toString();
  }

  public boolean hasNonNull(String column) {
    return _data.hasNonNull(column);
  }
}
