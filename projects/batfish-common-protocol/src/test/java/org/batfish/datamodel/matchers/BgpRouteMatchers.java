package org.batfish.datamodel.matchers;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasCommunities;
import org.hamcrest.Matcher;

public final class BgpRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.datamodel.BgpRoute}'s communities.
   */
  public static @Nonnull HasCommunities hasCommunities(
      @Nonnull Matcher<? super Set<Long>> subMatcher) {
    return new HasCommunities(subMatcher);
  }

  private BgpRouteMatchers() {}
}
