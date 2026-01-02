package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import org.batfish.datamodel.Zone;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class ZoneMatchers {

  static final class HasInterfaces extends FeatureMatcher<Zone, SortedSet<String>> {

    public HasInterfaces(Matcher<? super SortedSet<String>> subMatcher) {
      super(subMatcher, "A zone with interfaces:", "interfaces");
    }

    @Override
    protected SortedSet<String> featureValueOf(Zone actual) {
      return actual.getInterfaces();
    }
  }
}
