package org.batfish.datamodel.matchers;

import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.FibMatchersImpl.HasNextHopInterfaces;
import org.hamcrest.Matcher;

public class FibMatchers {
  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Fib}'s
   * nextHopInterfaces.
   */
  public static HasNextHopInterfaces hasNextHopInterfaces(
      Matcher<? super Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>> subMatcher) {
    return new HasNextHopInterfaces(subMatcher);
  }

  private FibMatchers() {}
}
