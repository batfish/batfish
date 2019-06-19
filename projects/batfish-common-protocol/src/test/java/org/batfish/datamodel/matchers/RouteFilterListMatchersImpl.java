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

final class RouteFilterListMatchersImpl {

  static class Permits extends TypeSafeDiagnosingMatcher<RouteFilterList> {

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

  static class Rejects extends TypeSafeDiagnosingMatcher<RouteFilterList> {

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

  static class HasLines extends FeatureMatcher<RouteFilterList, List<RouteFilterLine>> {

    public HasLines(Matcher<? super List<RouteFilterLine>> subMatcher) {
      super(subMatcher, "a routeFilterList with lines:", "lines");
    }

    @Override
    protected List<RouteFilterLine> featureValueOf(RouteFilterList actual) {
      return actual.getLines();
    }
  }

  private RouteFilterListMatchersImpl() {}
}
