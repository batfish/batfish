package org.batfish.datamodel.matchers;

import static org.batfish.datamodel.matchers.FibActionMatchersImpl.HasInterfaceName;
import static org.batfish.datamodel.matchers.FibActionMatchersImpl.IsFibForwardActionThat;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FibAction;
import org.batfish.datamodel.FibForward;
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
}
