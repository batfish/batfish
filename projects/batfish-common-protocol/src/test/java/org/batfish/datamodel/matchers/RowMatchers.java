package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.type.TypeReference;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.matchers.RowMatchersImpl.HasColumn;
import org.batfish.datamodel.table.Row;
import org.hamcrest.Matcher;

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
}
