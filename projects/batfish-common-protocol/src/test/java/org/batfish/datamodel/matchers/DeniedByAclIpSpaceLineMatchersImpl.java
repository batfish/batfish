package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.DeniedByAclIpSpaceLine;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DeniedByAclIpSpaceLineMatchersImpl {
  static final class HasIndex extends FeatureMatcher<DeniedByAclIpSpaceLine, Integer> {
    HasIndex(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "a DeniedByAclIpSpaceLine with index:", "index");
    }

    @Override
    protected Integer featureValueOf(DeniedByAclIpSpaceLine actual) {
      return actual.getIndex();
    }
  }

  static final class HasLineDescription extends FeatureMatcher<DeniedByAclIpSpaceLine, String> {
    public HasLineDescription(Matcher<? super String> subMatcher) {
      super(subMatcher, "a DeniedByAclIpSpaceLine with lineDescription:", "lineDescription");
    }

    @Override
    protected String featureValueOf(DeniedByAclIpSpaceLine actual) {
      return actual.getLineDescription();
    }
  }

  static final class HasName extends FeatureMatcher<DeniedByAclIpSpaceLine, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a DeniedByAclIpSpaceLine with name:", "name");
    }

    @Override
    protected String featureValueOf(DeniedByAclIpSpaceLine actual) {
      return actual.getName();
    }
  }

  static final class IsDeniedByAclIpSpaceLineThat
      extends IsInstanceThat<TraceEvent, DeniedByAclIpSpaceLine> {
    IsDeniedByAclIpSpaceLineThat(Matcher<? super DeniedByAclIpSpaceLine> subMatcher) {
      super(DeniedByAclIpSpaceLine.class, subMatcher);
    }
  }
}
