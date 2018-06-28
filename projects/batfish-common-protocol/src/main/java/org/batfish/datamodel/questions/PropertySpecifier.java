package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;
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
  static List<AutocompleteSuggestion> baseAutoComplete(String query, Set<String> allProperties) {

    String finalQuery = firstNonNull(query, "").toLowerCase();
    List<AutocompleteSuggestion> suggestions = new LinkedList<>();
    String queryWithStars = ".*" + (finalQuery.isEmpty() ? "" : finalQuery + ".*");
    Pattern queryPattern = safeGetPattern(queryWithStars);

    /*
     * if queryWithStars is not a valid Pattern, finalQuery must be a funky string that will not
     * match anything as string.contains or regex.matches; so we skip formalities altogether
     */
    if (queryPattern != null) {
      // 1. check if the pattern matches anything
      List<AutocompleteSuggestion> propertySuggestions =
          allProperties
              .stream()
              .filter(prop -> queryPattern.matcher(prop).matches())
              .map(prop -> new AutocompleteSuggestion(prop, false))
              .collect(Collectors.toList());

      // 2. if it did, add the pattern itself as the first suggestion
      if (!propertySuggestions.isEmpty()) {
        suggestions.add(
            new AutocompleteSuggestion(
                queryWithStars, false, "All properties matching regex " + queryWithStars));
      }

      // 3. then add the concrete suggestions
      suggestions.addAll(propertySuggestions);
    }
    return suggestions;
  }

  /** Converts the extracted propertyValue to what is specified in the properyDescriptor */
  static Object convertTypeIfNeeded(
      Object propertyValue, PropertyDescriptor<?> propertyDescriptor) {

    Object outputPropertyValue = propertyValue;

    // for Maps (e.g., routing policies) we use the list of keys
    if (outputPropertyValue instanceof Map<?, ?>) {
      outputPropertyValue =
          ((Map<?, ?>) outputPropertyValue)
              .keySet()
              .stream()
              .map(Object::toString)
              .collect(Collectors.toSet());
    }

    // check if a conversion to String is needed for complex objects (e.g., VRF)
    if (propertyDescriptor.getSchema().equals(Schema.STRING)
        && outputPropertyValue != null
        && !(outputPropertyValue instanceof String)) {
      if (outputPropertyValue instanceof ComparableStructure) {
        outputPropertyValue = ((ComparableStructure<?>) outputPropertyValue).getName();
      } else {
        outputPropertyValue = outputPropertyValue.toString();
      }
    }

    return outputPropertyValue;
  }

  /**
   * Uses {@code propertyDescriptor} to extract the property value from {@code object} and insert
   * into {@row} at {@code columnName}.
   *
   * @throws ClassCastException if the recovered property value is not compatible with the specified
   *     {@link Schema} in the {@code propertyDescriptor}.
   */
  public static <T> void fillProperty(
      PropertyDescriptor<T> propertyDescriptor, T object, String columnName, RowBuilder row) {
    Object propertyValue = propertyDescriptor.getGetter().apply(object);
    propertyValue = PropertySpecifier.convertTypeIfNeeded(propertyValue, propertyDescriptor);
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
