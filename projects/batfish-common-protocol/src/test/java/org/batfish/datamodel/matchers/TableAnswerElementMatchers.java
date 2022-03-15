package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.TableAnswerElementMatchersImpl.HasRows;
import org.batfish.datamodel.matchers.TableAnswerElementMatchersImpl.HasWarnings;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
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
}
