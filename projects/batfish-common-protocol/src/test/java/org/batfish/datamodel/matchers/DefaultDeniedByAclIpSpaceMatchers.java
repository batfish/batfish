package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.DefaultDeniedByAclIpSpace;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DefaultDeniedByAclIpSpaceMatchers {
  static final class HasName extends FeatureMatcher<DefaultDeniedByAclIpSpace, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "an DefaultDeniedByAclIpSpace with name:", "name");
    }

    @Override
    protected String featureValueOf(DefaultDeniedByAclIpSpace actual) {
      return actual.getName();
    }
  }

  static final class IsDefaultDeniedByAclIpSpaceThat
      extends IsInstanceThat<TraceEvent, DefaultDeniedByAclIpSpace> {
    IsDefaultDeniedByAclIpSpaceThat(Matcher<? super DefaultDeniedByAclIpSpace> subMatcher) {
      super(DefaultDeniedByAclIpSpace.class, subMatcher);
    }
  }
}
