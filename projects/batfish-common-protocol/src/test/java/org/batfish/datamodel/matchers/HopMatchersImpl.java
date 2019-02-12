package org.batfish.datamodel.matchers;

import java.util.List;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
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

  static class HasSteps extends FeatureMatcher<Hop, List<Step<?>>> {
    HasSteps(Matcher<? super List<Step<?>>> subMatcher) {
      super(subMatcher, "a Hop with steps:", "steps");
    }

    @Override
    protected List<Step<?>> featureValueOf(Hop hop) {
      return hop.getSteps();
    }
  }
}
