package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterLine;
import org.batfish.datamodel.Route6FilterList;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
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

  static class Rejects extends TypeSafeDiagnosingMatcher<Route6FilterList> {

    private final Prefix6 _prefix;

    Rejects(@Nonnull Prefix6 prefix) {
      _prefix = prefix;
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(
          String.format("A Route6FilterList that rejects prefix: '%s'", _prefix));
    }

    @Override
    protected boolean matchesSafely(Route6FilterList item, Description mismatchDescription) {
      if (!item.permits(_prefix)) {
        return true;
      }
      mismatchDescription.appendText(String.format("does not reject prefix '%s'", _prefix));
      return false;
    }
  }

  static class HasLines extends FeatureMatcher<Route6FilterList, List<Route6FilterLine>> {

    public HasLines(Matcher<? super List<Route6FilterLine>> subMatcher) {
      super(subMatcher, "a route6FilterList with lines:", "lines");
    }

    @Override
    protected List<Route6FilterLine> featureValueOf(Route6FilterList actual) {
      return actual.getLines();
    }
  }

  private Route6FilterListMatchersImpl() {}
}
