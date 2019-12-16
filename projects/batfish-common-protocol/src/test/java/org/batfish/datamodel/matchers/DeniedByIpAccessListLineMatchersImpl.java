package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.DeniedByAclLine;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DeniedByIpAccessListLineMatchersImpl {
  static final class HasIndex extends FeatureMatcher<DeniedByAclLine, Integer> {
    HasIndex(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "a DeniedByAclLine with index:", "index");
    }

    @Override
    protected Integer featureValueOf(DeniedByAclLine actual) {
      return actual.getIndex();
    }
  }

  static final class HasLineDescription extends FeatureMatcher<DeniedByAclLine, String> {
    public HasLineDescription(Matcher<? super String> subMatcher) {
      super(subMatcher, "a DeniedByAclLine with lineDescription:", "lineDescription");
    }

    @Override
    protected String featureValueOf(DeniedByAclLine actual) {
      return actual.getLineDescription();
    }
  }

  static final class HasName extends FeatureMatcher<DeniedByAclLine, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a DeniedByAclLine with name:", "name");
    }

    @Override
    protected String featureValueOf(DeniedByAclLine actual) {
      return actual.getName();
    }
  }

  static final class IsDeniedByAclLineThat extends IsInstanceThat<TraceEvent, DeniedByAclLine> {
    IsDeniedByAclLineThat(Matcher<? super DeniedByAclLine> subMatcher) {
      super(DeniedByAclLine.class, subMatcher);
    }
  }
}
