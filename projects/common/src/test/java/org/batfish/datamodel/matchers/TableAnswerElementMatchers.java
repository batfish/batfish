package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class TableAnswerElementMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * TableAnswerElement}'s {@code rows}.
   */
  public static Matcher<TableAnswerElement> hasRows(
      @Nonnull Matcher<? super Iterable<Row>> subMatcher) {
    return new HasRows(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * TableAnswerElement TableAnswerElement's} {@link TableAnswerElement#getWarnings() warnings}.
   */
  public static Matcher<TableAnswerElement> hasWarnings(
      @Nonnull Matcher<? super Iterable<String>> subMatcher) {
    return new HasWarnings(subMatcher);
  }

  private TableAnswerElementMatchers() {}

  private static final class HasRows extends FeatureMatcher<TableAnswerElement, Iterable<Row>> {
    public HasRows(@Nonnull Matcher<? super Iterable<Row>> subMatcher) {
      super(subMatcher, "TableAnswerElement with rows:", "rows");
    }

    @Override
    protected Iterable<Row> featureValueOf(TableAnswerElement actual) {
      return () -> actual.getRows().iterator();
    }
  }

  private static final class HasWarnings
      extends FeatureMatcher<TableAnswerElement, Iterable<String>> {
    public HasWarnings(@Nonnull Matcher<? super Iterable<String>> subMatcher) {
      super(subMatcher, "TableAnswerElement with warnings:", "warnings");
    }

    @Override
    protected Iterable<String> featureValueOf(TableAnswerElement tableAnswerElement) {
      return tableAnswerElement.getWarnings();
    }
  }
}
