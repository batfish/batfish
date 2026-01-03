package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.Schema.Type;
import org.batfish.datamodel.table.Row.RowBuilder;

/** An abstract class that contains properties of various entities in the network */
public abstract class PropertySpecifier {

  /** A class that describes an individual property and how to get it */
  @ParametersAreNonnullByDefault
  public static class PropertyDescriptor<T> {
    @Nonnull Function<T, Object> _getter;
    @Nonnull Schema _schema;
    @Nonnull String _description;

    public PropertyDescriptor(Function<T, Object> getter, Schema schema, String description) {
      _getter = getter;
      _schema = schema;
      _description = description;
    }

    public @Nonnull String getDescription() {
      return _description;
    }

    public @Nonnull Function<T, Object> getGetter() {
      return _getter;
    }

    public @Nonnull Schema getSchema() {
      return _schema;
    }
  }

  /** Converts {@code propertyValue} to {@code targetSchema} if needed */
  public static Object convertTypeIfNeeded(Object propertyValue, Schema targetSchema) {

    Object outputPropertyValue = propertyValue;

    // for Maps (e.g., routing policies) we use the set of keys
    if (outputPropertyValue instanceof Map<?, ?>) {
      outputPropertyValue =
          ((Map<?, ?>) outputPropertyValue)
              .keySet().stream().map(Object::toString).collect(ImmutableSet.toImmutableSet());
    }

    // check if a conversion to String or List/Set<String> is needed for complex objects (e.g., VRF)
    if (targetSchema.equals(Schema.STRING)
        && outputPropertyValue != null
        && !(outputPropertyValue instanceof String)) {
      if (outputPropertyValue instanceof ComparableStructure) {
        outputPropertyValue = ((ComparableStructure<?>) outputPropertyValue).getName();
      } else {
        outputPropertyValue = outputPropertyValue.toString();
      }
    } else if ((targetSchema.equals(Schema.list(Schema.STRING))
            || targetSchema.equals(Schema.set(Schema.STRING)))
        && outputPropertyValue instanceof Collection<?>) {
      Collection<?> outputCollection = (Collection<?>) outputPropertyValue;
      if (!outputCollection.isEmpty() && !(outputCollection.iterator().next() instanceof String)) {
        Stream<?> stream =
            outputCollection.stream()
                .map(
                    e ->
                        (e instanceof ComparableStructure)
                            ? ((ComparableStructure<?>) e).getName()
                            : e.toString());
        outputPropertyValue =
            targetSchema.getType() == Type.LIST
                ? stream.collect(ImmutableList.toImmutableList())
                : stream.collect(ImmutableSet.toImmutableSet());
      }
    }

    return outputPropertyValue;
  }

  /**
   * Uses {@code propertyDescriptor} to extract the property value from {@code object} and insert
   * into {@code row} at {@code columnName}.
   *
   * @throws ClassCastException if the recovered property value is not compatible with the specified
   *     {@link Schema} in the {@code propertyDescriptor}.
   */
  public static <T> void fillProperty(
      PropertyDescriptor<T> propertyDescriptor, T object, String columnName, RowBuilder row) {
    checkArgument(propertyDescriptor != null, "'propertyDescriptor' cannot be null");
    checkArgument(object != null, "'object' cannot be null");
    checkArgument(columnName != null, "'columnName' cannot be null");
    checkArgument(row != null, "'row' cannot be null");

    Object propertyValue = propertyDescriptor.getGetter().apply(object);
    propertyValue =
        PropertySpecifier.convertTypeIfNeeded(propertyValue, propertyDescriptor.getSchema());
    fillProperty(columnName, propertyValue, row, propertyDescriptor); // separate for testing
  }

  @VisibleForTesting
  static void fillProperty(
      String columnName,
      Object propertyValue,
      RowBuilder row,
      PropertyDescriptor<?> propertyDescriptor) {
    row.put(columnName, propertyValue);
    // if this barfs, the value cannot be converted to expected Schema
    row.build().get(columnName, propertyDescriptor.getSchema());
  }

  /**
   * Returns a list of the names of all properties that match this specifier object, in preference
   * order.
   *
   * <p>Note: the returned list is expected to have unique entries.
   */
  public abstract List<String> getMatchingProperties();
}
