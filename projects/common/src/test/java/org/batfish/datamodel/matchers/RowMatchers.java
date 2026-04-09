package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.Row;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class RowMatchers {

  /**
   * Provides a matcher that matches if the {@link Row} has a column whose name is matched by {@code
   * keyMatcher} and whose value is matched by {@code valueMatcher}. Caller should pass in a {@link
   * TypeReference} suitable for use by {@link Row#get(String, TypeReference)}.
   */
  public static @Nonnull <V> Matcher<Row> hasColumn(
      @Nonnull Matcher<? super String> keyMatcher,
      @Nonnull Matcher<? super V> valueMatcher,
      @Nonnull Schema schema) {
    return new HasColumn<>(keyMatcher, valueMatcher, schema);
  }

  /**
   * Provides a matcher that matches if the {@link Row} has a column named {@code key} whose value
   * is matched by {@code valueMatcher}. Caller should pass in a {@link TypeReference} suitable for
   * use by {@link Row#get(String, TypeReference)}.
   */
  public static @Nonnull <V> Matcher<Row> hasColumn(
      String key, Matcher<? super V> valueMatcher, Schema schema) {
    return new HasColumn<>(equalTo(key), valueMatcher, schema);
  }

  /**
   * Provides a matcher that matches if the {@link Row} has a column named {@code key} whose value
   * is equal to {@code expectedValue}.
   */
  public static @Nonnull Matcher<Row> hasColumn(
      String key, @Nonnull Object expectedValue, Schema schema) {
    return hasColumn(equalTo(key), equalTo(expectedValue), schema);
  }

  private RowMatchers() {}

  private static final class HasColumn<V> extends TypeSafeDiagnosingMatcher<Row> {
    private final Matcher<? super String> _keyMatcher;
    private final Matcher<? super V> _valueMatcher;
    private final Schema _schema;

    public HasColumn(
        @Nonnull Matcher<? super String> keyMatcher,
        @Nonnull Matcher<? super V> valueMatcher,
        Schema schema) {
      _keyMatcher = keyMatcher;
      _valueMatcher = valueMatcher;
      _schema = schema;
    }

    @Override
    public void describeTo(Description description) {
      description
          .appendText("A row with a column with name matching: ")
          .appendDescriptionOf(_keyMatcher)
          .appendText(" and value matching: ")
          .appendDescriptionOf(_valueMatcher);
    }

    @Override
    protected boolean matchesSafely(Row item, Description mismatchDescription) {
      Collection<String> columnNames = item.getColumnNames();
      Optional<String> columnNameOption =
          columnNames.stream().filter(_keyMatcher::matches).findFirst();
      if (!columnNameOption.isPresent()) {
        mismatchDescription.appendText("No column of row matched: ");
        hasItem(_keyMatcher).describeMismatch(columnNames, mismatchDescription);
        return false;
      }
      String columnName = columnNameOption.get();
      Object value;
      try {
        value = item.get(columnName, _schema);
      } catch (Exception e) {
        mismatchDescription.appendText(
            String.format(
                "Value for column '%s' could not be converted to a '%s': %s",
                columnName, _schema, Throwables.getStackTraceAsString(e)));
        return false;
      }
      if (!_valueMatcher.matches(value)) {
        mismatchDescription.appendText(
            String.format("Value for column '%s' did not match: ", columnName));
        _valueMatcher.describeMismatch(value, mismatchDescription);
        return false;
      }
      return true;
    }
  }
}
