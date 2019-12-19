package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.matchers.AclLineMatchersImpl.IsExprAclLineThat;
import org.hamcrest.Matcher;

public class AclLineMatchers {

  /**
   * Provides a matcher that matches {@link ExprAclLine}s that match the provided {@code
   * subMatcher}.
   */
  public static IsExprAclLineThat isExprAclLineThat(
      @Nonnull Matcher<? super ExprAclLine> subMatcher) {
    return new IsExprAclLineThat(subMatcher);
  }
}
