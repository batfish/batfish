package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.RowsMatchersImpl.HasSize;
import org.batfish.datamodel.table.Rows;
import org.hamcrest.Matcher;

public final class RowsMatchers {

  /**
   * Provides a matcher that matches if the {@link Rows}'s {@code size} is equal to {@code
   * expectedSize}.
   */
  public static @Nonnull Matcher<Rows> hasSize(int expectedSize) {
    return new HasSize(equalTo(expectedSize));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Rows}'s
   * {@code size}.
   */
  public static @Nonnull Matcher<Rows> hasSize(@Nonnull Matcher<? super Integer> subMatcher) {
    return new HasSize(subMatcher);
  }

  private RowsMatchers() {}
}
