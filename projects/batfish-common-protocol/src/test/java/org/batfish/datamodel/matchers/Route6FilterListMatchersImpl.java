package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

final class Route6FilterListMatchersImpl {

  static class Permits extends TypeSafeDiagnosingMatcher<Route6FilterList> {

    private final Prefix6 _prefix;

    Permits(@Nonnull Prefix6 prefix) {
      _prefix = prefix;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format("A Route6FilterList that permits prefix: '%s'", _prefix));
    }

    @Override
    protected boolean matchesSafely(Route6FilterList item, Description mismatchDescription) {
      if (item.permits(_prefix)) {
        return true;
      }
      mismatchDescription.appendText(String.format("does not permit prefix '%s'", _prefix));
      return false;
    }
  }

  private Route6FilterListMatchersImpl() {}
}
