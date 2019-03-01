package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.AbstractRouteMatchersImpl.HasNetwork;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class AbstractRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRoute}'s network.
   */
  public static @Nonnull Matcher<AbstractRoute> hasNetwork(Matcher<? super Prefix> subMatcher) {
    return new HasNetwork(subMatcher);
  }

  /**
   * Provides a matcher that matches when {@link AbstractRoute}'s network is {@code
   * expectedNetwork}.
   */
  public static @Nonnull Matcher<AbstractRoute> hasNetwork(Prefix expectedNetwork) {
    return new HasNetwork(equalTo(expectedNetwork));
  }

  private AbstractRouteMatchers() {}
}
