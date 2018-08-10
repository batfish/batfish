package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.PermittedByAclIpSpaceLine;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class PermittedByAclIpSpaceLineMatchersImpl {
  static final class HasIndex extends FeatureMatcher<PermittedByAclIpSpaceLine, Integer> {
    HasIndex(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "a PermittedByAclIpSpaceLine with index:", "index");
    }

    @Override
    protected Integer featureValueOf(PermittedByAclIpSpaceLine actual) {
      return actual.getIndex();
    }
  }

  static final class HasLineDescription extends FeatureMatcher<PermittedByAclIpSpaceLine, String> {
    public HasLineDescription(Matcher<? super String> subMatcher) {
      super(subMatcher, "a PermittedByAclIpSpaceLine with lineDescription:", "lineDescription");
    }

    @Override
    protected String featureValueOf(PermittedByAclIpSpaceLine actual) {
      return actual.getLineDescription();
    }
  }

  static final class HasName extends FeatureMatcher<PermittedByAclIpSpaceLine, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a PermittedByAclIpSpaceLine with name:", "name");
    }

    @Override
    protected String featureValueOf(PermittedByAclIpSpaceLine actual) {
      return actual.getName();
    }
  }

  static final class IsPermittedByAclIpSpaceLineThat
      extends IsInstanceThat<TraceEvent, PermittedByAclIpSpaceLine> {
    IsPermittedByAclIpSpaceLineThat(Matcher<? super PermittedByAclIpSpaceLine> subMatcher) {
      super(PermittedByAclIpSpaceLine.class, subMatcher);
    }
  }
}
