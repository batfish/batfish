package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class PermittedByAclMatchers {

  public static @Nonnull Matcher<PermittedByAcl> hasAclName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasAclName(subMatcher);
  }

  public static @Nonnull Matcher<PermittedByAcl> hasAclName(@Nonnull String name) {
    return hasAclName(org.hamcrest.Matchers.equalTo(name));
  }

  public static @Nonnull Matcher<AclLineMatchExpr> isPermittedByAclThat(
      @Nonnull Matcher<? super PermittedByAcl> subMatcher) {
    return new IsPermittedByAclThat(subMatcher);
  }

  private static class HasAclName extends FeatureMatcher<PermittedByAcl, String> {

    public HasAclName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A PermittedByAcl with aclName:", "aclName");
    }

    @Override
    protected String featureValueOf(PermittedByAcl actual) {
      return actual.getAclName();
    }
  }

  private static final class IsPermittedByAclThat
      extends IsInstanceThat<AclLineMatchExpr, PermittedByAcl> {
    IsPermittedByAclThat(@Nonnull Matcher<? super PermittedByAcl> subMatcher) {
      super(PermittedByAcl.class, subMatcher);
    }
  }

  private PermittedByAclMatchers() {}
}
