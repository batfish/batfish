package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterLine;
import org.batfish.datamodel.matchers.Route6FilterListMatchersImpl.HasLines;
import org.batfish.datamodel.matchers.Route6FilterListMatchersImpl.Permits;
import org.batfish.datamodel.matchers.Route6FilterListMatchersImpl.Rejects;
import org.hamcrest.Matcher;

public class Route6FilterListMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.Route6FilterList}'s lines.
   */
  public static HasLines hasLines(@Nonnull Matcher<? super List<Route6FilterLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.Route6FilterList} permits a
   * {@link Prefix6}
   */
  public static Permits permits(@Nonnull Prefix6 prefix) {
    return new Permits(prefix);
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.Route6FilterList} rejects a
   * {@link Prefix6}
   */
  public static Rejects rejects(@Nonnull Prefix6 prefix) {
    return new Rejects(prefix);
  }

  private Route6FilterListMatchers() {}
}
