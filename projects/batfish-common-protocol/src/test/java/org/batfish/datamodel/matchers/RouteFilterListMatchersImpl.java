package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.hamcrest.Description;
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

  private RouteFilterListMatchersImpl() {}
}
