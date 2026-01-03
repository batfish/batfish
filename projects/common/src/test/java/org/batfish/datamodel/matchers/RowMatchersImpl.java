package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.hasItem;

import com.google.common.base.Throwables;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.Row;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class RowMatchersImpl {

  static final class HasColumn<V> extends TypeSafeDiagnosingMatcher<Row> {
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

  private RowMatchersImpl() {}
}
