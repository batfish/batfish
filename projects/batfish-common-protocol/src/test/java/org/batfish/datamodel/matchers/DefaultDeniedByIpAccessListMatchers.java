package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.DefaultDeniedByIpAccessList;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DefaultDeniedByIpAccessListMatchers {
  static final class HasName extends FeatureMatcher<DefaultDeniedByIpAccessList, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "an DefaultDeniedByIpAccessList with name:", "name");
    }

    @Override
    protected String featureValueOf(DefaultDeniedByIpAccessList actual) {
      return actual.getName();
    }
  }

  static final class IsDefaultDeniedByIpAccessListThat
      extends IsInstanceThat<TraceEvent, DefaultDeniedByIpAccessList> {
    IsDefaultDeniedByIpAccessListThat(Matcher<? super DefaultDeniedByIpAccessList> subMatcher) {
      super(DefaultDeniedByIpAccessList.class, subMatcher);
    }
  }
}
