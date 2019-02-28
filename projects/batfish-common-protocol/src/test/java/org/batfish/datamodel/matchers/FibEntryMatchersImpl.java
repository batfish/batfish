package org.batfish.datamodel.matchers;

import org.batfish.datamodel.FibEntry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class FibEntryMatchersImpl {

  static final class HasInterface extends FeatureMatcher<FibEntry, String> {

    public HasInterface(Matcher<? super String> subMatcher) {
      super(subMatcher, "A FibEntry with: nextHopInterface", "nextHopInterface");
    }

    @Override
    protected String featureValueOf(FibEntry fibEntry) {
      return fibEntry.getInterfaceName();
    }
  }

  private FibEntryMatchersImpl() {}
}
