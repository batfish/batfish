package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.DeniedByNamedIpSpace;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DeniedByNamedIpSpaceMatchers {
  static final class HasName extends FeatureMatcher<DeniedByNamedIpSpace, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "an DeniedByNamedIpSpace with name:", "name");
    }

    @Override
    protected String featureValueOf(DeniedByNamedIpSpace actual) {
      return actual.getName();
    }
  }

  static final class IsDeniedByNamedIpSpaceThat
      extends IsInstanceThat<TraceEvent, DeniedByNamedIpSpace> {
    IsDeniedByNamedIpSpaceThat(Matcher<? super DeniedByNamedIpSpace> subMatcher) {
      super(DeniedByNamedIpSpace.class, subMatcher);
    }
  }
}
