package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.matchers.IpAccessListLineMatchersImpl.HasMatchCondition;
import org.hamcrest.Matcher;

public class IpAccessListLineMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the HeaderSpace's
   * dstIps.
   */
  public static HasMatchCondition hasMatchCondition(
      @Nonnull Matcher<? super AclLineMatchExpr> subMatcher) {
    return new HasMatchCondition(subMatcher);
  }

  private IpAccessListLineMatchers() {}
}
