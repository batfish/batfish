package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class RouteFilterListMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * org.batfish.datamodel.RouteFilterList}'s lines.
   */
  public static Matcher<RouteFilterList> hasLines(
      @Nonnull Matcher<? super List<RouteFilterLine>> subMatcher) {
    return new HasLines(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.RouteFilterList} permits a
   * {@link Prefix}
   */
  public static Matcher<RouteFilterList> permits(@Nonnull Prefix prefix) {
    return new Permits(prefix);
  }

  /**
   * Provides a matcher that matches if the {@link org.batfish.datamodel.RouteFilterList} rejects a
   * {@link Prefix}
   */
  public static Matcher<RouteFilterList> rejects(@Nonnull Prefix prefix) {
    return new Rejects(prefix);
  }

  private RouteFilterListMatchers() {}

  private static final class Permits extends TypeSafeDiagnosingMatcher<RouteFilterList> {

    private final Prefix _prefix;

    Permits(@Nonnull Prefix prefix) {
      _prefix = prefix;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("A RouteFilterList that permits prefix: '%s'", _prefix));
    }

    @Override
    protected boolean matchesSafely(RouteFilterList item, Description mismatchDescription) {
      if (item.permits(_prefix)) {
        return true;
      }
      mismatchDescription.appendText(String.format("does not permit prefix '%s'", _prefix));
      return false;
    }
  }

  private static final class Rejects extends TypeSafeDiagnosingMatcher<RouteFilterList> {

    private final Prefix _prefix;

    Rejects(@Nonnull Prefix prefix) {
      _prefix = prefix;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("A RouteFilterList that rejects prefix: '%s'", _prefix));
    }

    @Override
    protected boolean matchesSafely(RouteFilterList item, Description mismatchDescription) {
      if (!item.permits(_prefix)) {
        return true;
      }
      mismatchDescription.appendText(String.format("does not reject prefix '%s'", _prefix));
      return false;
    }
  }

  private static final class HasLines
      extends FeatureMatcher<RouteFilterList, List<RouteFilterLine>> {

    public HasLines(Matcher<? super List<RouteFilterLine>> subMatcher) {
      super(subMatcher, "a routeFilterList with lines:", "lines");
    }

    @Override
    protected List<RouteFilterLine> featureValueOf(RouteFilterList actual) {
      return actual.getLines();
    }
  }
}
