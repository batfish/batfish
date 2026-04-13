package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibForward;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link FibAction} */
@ParametersAreNonnullByDefault
public final class FibActionMatchers {

  /**
   * A matcher that matches when the {@link FibForward}'s interfaceName is {@code
   * expectedInterfaceName}.
   */
  public static @Nonnull Matcher<FibForward> hasInterfaceName(String expectedInterfaceName) {
    return new HasInterfaceName(equalTo(expectedInterfaceName));
  }

  /**
   * A matcher that matches when the {@link FibAction} is a {@link FibForward} matched by the
   * provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<FibAction> isFibForwardActionThat(
      Matcher<? super FibForward> subMatcher) {
    return new IsFibForwardActionThat(subMatcher);
  }

  private FibActionMatchers() {}

  private static final class HasInterfaceName extends FeatureMatcher<FibForward, String> {

    public HasInterfaceName(Matcher<? super String> subMatcher) {
      super(subMatcher, "A FibForward with interfaceName", "interfaceName");
    }

    @Override
    protected @Nonnull String featureValueOf(FibForward actual) {
      return actual.getInterfaceName();
    }
  }

  private static final class IsFibForwardActionThat extends IsInstanceThat<FibAction, FibForward> {

    IsFibForwardActionThat(Matcher<? super FibForward> subMatcher) {
      super(FibForward.class, subMatcher);
    }
  }
}
