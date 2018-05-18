package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.matchers.RouteFilterListMatchersImpl.HasLines;
import org.hamcrest.Matcher;

public class RouteFilterListMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.RouteFilterList}'s lines.
   */
  public static HasLines hasLines(@Nonnull Matcher<? super List<RouteFilterLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  private RouteFilterListMatchers() {}
}
