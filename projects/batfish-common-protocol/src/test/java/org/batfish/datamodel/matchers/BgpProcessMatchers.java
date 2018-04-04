package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasNeighbor;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasNeighbors;
import org.hamcrest.Matcher;

public class BgpProcessMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * neighbor with specified prefix.
   */
  public static HasNeighbor hasNeighbor(
      @Nonnull Prefix prefix, @Nonnull Matcher<? super BgpNeighbor> subMatcher) {
    return new HasNeighbor(prefix, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * neighbors.
   */
  public static HasNeighbors hasNeighbors(
      @Nonnull Matcher<? super Map<Prefix, BgpNeighbor>> subMatcher) {
    return new HasNeighbors(subMatcher);
  }
}
