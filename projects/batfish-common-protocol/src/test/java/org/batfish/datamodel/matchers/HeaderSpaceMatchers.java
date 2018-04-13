package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.State;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasDstIps;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasSrcIps;
import org.batfish.datamodel.matchers.HeaderSpaceMatchersImpl.HasState;
import org.hamcrest.Matcher;

public class HeaderSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * dstIps.
   */
  public static HasDstIps hasDstIps(@Nonnull Matcher<? super SortedSet<IpWildcard>> subMatcher) {
    return new HasDstIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * srcIps.
   */
  public static HasSrcIps hasSrcIps(@Nonnull Matcher<? super SortedSet<IpWildcard>> subMatcher) {
    return new HasSrcIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * state.
   */
  public static HasState hasState(@Nonnull Matcher<? super SortedSet<State>> subMatcher) {
    return new HasState(subMatcher);
  }

  private HeaderSpaceMatchers() {}
}
