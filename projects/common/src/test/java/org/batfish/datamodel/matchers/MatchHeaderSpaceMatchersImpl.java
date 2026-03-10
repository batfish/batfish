package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class MatchHeaderSpaceMatchersImpl {

  static class HasHeaderSpace extends FeatureMatcher<MatchHeaderSpace, HeaderSpace> {

    public HasHeaderSpace(@Nonnull Matcher<? super HeaderSpace> subMatcher) {
      super(subMatcher, "A MatchHeaderSpace with headerSpace:", "headerSpace");
    }

    @Override
    protected HeaderSpace featureValueOf(MatchHeaderSpace actual) {
      return actual.getHeaderspace();
    }
  }

  static class IsMatchHeaderSpaceThat extends IsInstanceThat<AclLineMatchExpr, MatchHeaderSpace> {
    IsMatchHeaderSpaceThat(@Nonnull Matcher<? super MatchHeaderSpace> subMatcher) {
      super(MatchHeaderSpace.class, subMatcher);
    }
  }

  private MatchHeaderSpaceMatchersImpl() {}
}
