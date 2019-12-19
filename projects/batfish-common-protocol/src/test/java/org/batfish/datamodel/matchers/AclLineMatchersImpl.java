package org.batfish.datamodel.matchers;

import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.hamcrest.Matcher;

public class AclLineMatchersImpl {

  static final class IsExprAclLineThat extends IsInstanceThat<AclLine, ExprAclLine> {
    IsExprAclLineThat(Matcher<? super ExprAclLine> subMatcher) {
      super(ExprAclLine.class, subMatcher);
    }
  }

  private AclLineMatchersImpl() {}
}
