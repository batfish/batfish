package org.batfish.datamodel.matchers;

import org.batfish.datamodel.flow.Hop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class HopMatchersImpl {
  private HopMatchersImpl() {}

  static class HasNodeName extends FeatureMatcher<Hop, String> {
    HasNodeName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a Hop with node name:", "node name");
    }

    @Override
    protected String featureValueOf(Hop hop) {
      return hop.getNode().getName();
    }
  }
}
