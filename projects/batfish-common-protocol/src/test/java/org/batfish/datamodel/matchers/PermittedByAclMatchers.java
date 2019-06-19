package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class PermittedByAclMatchers {

  static class HasAclName extends FeatureMatcher<PermittedByAcl, String> {

    public HasAclName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A PermittedByAcl with aclName:", "aclName");
    }

    @Override
    protected String featureValueOf(PermittedByAcl actual) {
      return actual.getAclName();
    }
  }

  static class IsPermittedByAclThat extends IsInstanceThat<AclLineMatchExpr, PermittedByAcl> {
    IsPermittedByAclThat(@Nonnull Matcher<? super PermittedByAcl> subMatcher) {
      super(PermittedByAcl.class, subMatcher);
    }
  }

  private PermittedByAclMatchers() {}
}
