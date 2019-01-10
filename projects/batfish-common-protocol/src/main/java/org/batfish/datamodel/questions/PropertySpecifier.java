package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.Schema.Type;
import org.batfish.datamodel.table.Row.RowBuilder;

public abstract class PropertySpecifier {

  @ParametersAreNonnullByDefault
  public static class PropertyDescriptor<T> {
    @Nonnull Function<T, Object> _getter;
    @Nonnull Schema _schema;

    public PropertyDescriptor(Function<T, Object> getter, Schema schema) {
      _getter = getter;
      _schema = schema;
    }

    public Function<T, Object> getGetter() {
      return _getter;
    }

    public Schema getSchema() {
      return _schema;
    }
  }

  /**
   * Returns a list of suggestions based on the query. The current implementation treats the query
   * as a substring of the property string.
   *
   * @param query The query that came to the concrete child class
   * @return The list of suggestions
   */
  static List<AutocompleteSuggestion> baseAutoComplete(
      @Nullable String query, Set<String> allProperties) {

    String finalQuery = firstNonNull(query, "").toLowerCase();
    ImmutableList.Builder<AutocompleteSuggestion> suggestions = new ImmutableList.Builder<>();
    String queryWithStars = ".*" + (finalQuery.isEmpty() ? "" : finalQuery + ".*");
    Pattern queryPattern = safeGetPattern(queryWithStars);

    /*
     * if queryWithStars is not a valid Pattern, finalQuery must be a funky string that will not
     * match anything as string.contains or regex.matches; so we skip formalities altogether
     */
    if (queryPattern != null) {
      suggestions.addAll(
          allProperties
              .stream()
              .filter(prop -> queryPattern.matcher(prop.toLowerCase()).matches())
              .map(prop -> new AutocompleteSuggestion(prop, false))
              .collect(Collectors.toList()));
    }
    return suggestions.build();
  }

  /** Converts {@code propertyValue} to {@code targetSchema} if needed */
  public static Object convertTypeIfNeeded(Object propertyValue, Schema targetSchema) {

    Object outputPropertyValue = propertyValue;

    // for Maps (e.g., routing policies) we use the set of keys
    if (outputPropertyValue instanceof Map<?, ?>) {
      outputPropertyValue =
          ((Map<?, ?>) outputPropertyValue)
              .keySet()
              .stream()
              .map(Object::toString)
              .collect(Collectors.toSet());
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
        && outputPropertyValue != null
        && outputPropertyValue instanceof Collection<?>) {
      Collection<?> outputCollection = (Collection<?>) outputPropertyValue;
      if (!outputCollection.isEmpty() && !(outputCollection.iterator().next() instanceof String)) {
        Stream<?> stream =
            outputCollection
                .stream()
                .map(
                    e ->
                        (e instanceof ComparableStructure)
                            ? ((ComparableStructure<?>) e).getName()
                            : e.toString());
        outputPropertyValue =
            targetSchema.getType() == Type.LIST
                ? stream.collect(Collectors.toList())
                : stream.collect(Collectors.toSet());
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
   * Returns all properties that match this specifier object
   *
   * @return The matching set
   */
  public abstract Set<String> getMatchingProperties();

  /** Returns the Pattern if {@code candidateRegex} is a valid regex, and null otherwise */
  private static Pattern safeGetPattern(String candidateRegex) {
    try {
      return Pattern.compile(candidateRegex);
    } catch (PatternSyntaxException e) {
      return null;
    }
  }
}
