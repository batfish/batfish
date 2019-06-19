package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.PermittedByNamedIpSpace;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class PermittedByNamedIpSpaceMatchers {
  static final class HasName extends FeatureMatcher<PermittedByNamedIpSpace, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "an PermittedByNamedIpSpace with name:", "name");
    }

    @Override
    protected String featureValueOf(PermittedByNamedIpSpace actual) {
      return actual.getName();
    }
  }

  static final class IsPermittedByNamedIpSpaceThat
      extends IsInstanceThat<TraceEvent, PermittedByNamedIpSpace> {
    IsPermittedByNamedIpSpaceThat(Matcher<? super PermittedByNamedIpSpace> subMatcher) {
      super(PermittedByNamedIpSpace.class, subMatcher);
    }
  }
}
