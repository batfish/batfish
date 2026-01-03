package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibForward;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class FibActionMatchersImpl {

  static final class HasInterfaceName extends FeatureMatcher<FibForward, String> {

    public HasInterfaceName(Matcher<? super String> subMatcher) {
      super(subMatcher, "A FibForward with interfaceName", "interfaceName");
    }

    @Override
    protected @Nonnull String featureValueOf(FibForward actual) {
      return actual.getInterfaceName();
    }
  }

  static final class IsFibForwardActionThat extends IsInstanceThat<FibAction, FibForward> {

    IsFibForwardActionThat(Matcher<? super FibForward> subMatcher) {
      super(FibForward.class, subMatcher);
    }
  }

  private FibActionMatchersImpl() {}
}
