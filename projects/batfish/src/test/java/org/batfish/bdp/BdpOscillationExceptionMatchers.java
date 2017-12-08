package org.batfish.bdp;

import org.batfish.common.BdpOscillationException;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class BdpOscillationExceptionMatchers {
  private static class HasMessage extends FeatureMatcher<BdpOscillationException, String> {

    public HasMessage(Matcher<? super String> subMatcher) {
      super(subMatcher, "message", "message");
    }

    @Override
    protected String featureValueOf(BdpOscillationException actual) {
      return actual.getMessage();
    }
  }

  public static HasMessage hasMessage(Matcher<? super String> subMatcher) {
    return new HasMessage(subMatcher);
  }
}
