package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.matchers.MatchHeaderSpaceMatchersImpl.HasHeaderSpace;
import org.batfish.datamodel.matchers.MatchHeaderSpaceMatchersImpl.IsMatchHeaderSpaceThat;
import org.hamcrest.Matcher;

public class MatchHeaderSpaceMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the
   * MatchHeaderSpace's headerSpace.
   */
  public static HasHeaderSpace hasHeaderSpace(Matcher<? super HeaderSpace> subMatcher) {
    return new HasHeaderSpace(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link MatchHeaderSpace} matched by the
   * provided {@code subMatcher}.
   */
  public static IsMatchHeaderSpaceThat isMatchHeaderSpaceThat(
      @Nonnull Matcher<? super MatchHeaderSpace> subMatcher) {
    return new IsMatchHeaderSpaceThat(subMatcher);
  }

  private MatchHeaderSpaceMatchers() {}
}
