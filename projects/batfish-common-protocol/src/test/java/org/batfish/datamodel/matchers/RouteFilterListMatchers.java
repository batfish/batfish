package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.matchers.RouteFilterListMatchersImpl.HasLines;
import org.batfish.datamodel.matchers.RouteFilterListMatchersImpl.Permits;
import org.batfish.datamodel.matchers.RouteFilterListMatchersImpl.Rejects;
import org.hamcrest.Matcher;

public class RouteFilterListMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.RouteFilterList}'s lines.
   */
  public static HasLines hasLines(@Nonnull Matcher<? super List<RouteFilterLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.RouteFilterList} permits a
   * {@link Prefix}
   */
  public static Permits permits(@Nonnull Prefix prefix) {
    return new Permits(prefix);
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.RouteFilterList} rejects a
   * {@link Prefix}
   */
  public static Rejects rejects(@Nonnull Prefix prefix) {
    return new Rejects(prefix);
  }

  private RouteFilterListMatchers() {}
}
