package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
  public static class PropertyDescriptor<T extends Object> {
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
   * @param childClass The child class
   * @return The list of suggestions
   */
  protected static List<AutocompleteSuggestion> baseAutoComplete(
      String query, Class<? extends PropertySpecifier> childClass, Set<String> allProperties) {

    String finalQuery = firstNonNull(query, "").toLowerCase();
    List<AutocompleteSuggestion> suggestions = new LinkedList<>();
    String queryWithStars = ".*" + (finalQuery.isEmpty() ? "" : finalQuery + ".*");
    Pattern queryPattern = safeGetPattern(queryWithStars);

    /**
     * if queryWithStars is not a valid Pattern, finalQuery must be a funky string that will not
     * match anything as string.contains or regex.matches; so we skip formalities altogether
     */
    if (queryPattern != null) {

      // first add queryWithStars if the original query wasn't a regex and it has non-empty matches
      if (!isRegex(finalQuery)) {
        boolean queryWithStarsHasMatches = false;
        try {
          Constructor<?> constructor = childClass.getConstructor(String.class);
          PropertySpecifier instance = (PropertySpecifier) constructor.newInstance(queryWithStars);
          queryWithStarsHasMatches = !instance.getMatchingProperties().isEmpty();
        } catch (NoSuchMethodException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException e) {
          // do nothing
        }

        if (queryWithStarsHasMatches) {
          suggestions.add(
              new AutocompleteSuggestion(
                  queryWithStars, true, "All properties matching regex " + queryWithStars));
        }
      }

      // now add all properties that contain the query
      suggestions.addAll(
          allProperties
              .stream()
              .filter(prop -> queryPattern.matcher(prop).matches())
              .map(prop -> new AutocompleteSuggestion(prop, false))
              .collect(Collectors.toList()));
    }
    return suggestions;
  }

  public static Object convertTypeIfNeeded(
      Object propertyValue, PropertyDescriptor<?> propertyDescriptor) {

    // for Maps (e.g., routing policies) we use the list of keys
    if (propertyValue instanceof Map<?, ?>) {
      propertyValue =
          ((Map<?, ?>) propertyValue)
              .keySet()
              .stream()
              .map(k -> k.toString())
              .collect(Collectors.toSet());
    }

    // check if a conversion to String is needed for complex objects (e.g., VRF)
    if (propertyDescriptor.getSchema().equals(Schema.STRING)
        && propertyValue != null
        && !(propertyValue instanceof String)) {
      if (propertyValue instanceof ComparableStructure) {
        propertyValue = ((ComparableStructure<?>) propertyValue).getName();
      } else {
        propertyValue = propertyValue.toString();
      }
    }

    return propertyValue;
  }

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

  // hacky way to check if the query is a regex already
  private static boolean isRegex(String query) {
    return query.endsWith("*") || query.startsWith(".*");
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
