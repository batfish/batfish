package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.DeniedByIpAccessListLine;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class DeniedByIpAccessListLineMatchersImpl {
  static final class HasIndex extends FeatureMatcher<DeniedByIpAccessListLine, Integer> {
    HasIndex(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "a DeniedByIpAccessListLine with index:", "index");
    }

    @Override
    protected Integer featureValueOf(DeniedByIpAccessListLine actual) {
      return actual.getIndex();
    }
  }

  static final class HasLineDescription extends FeatureMatcher<DeniedByIpAccessListLine, String> {
    public HasLineDescription(Matcher<? super String> subMatcher) {
      super(subMatcher, "a DeniedByIpAccessListLine with lineDescription:", "lineDescription");
    }

    @Override
    protected String featureValueOf(DeniedByIpAccessListLine actual) {
      return actual.getLineDescription();
    }
  }

  static final class HasName extends FeatureMatcher<DeniedByIpAccessListLine, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a DeniedByIpAccessListLine with name:", "name");
    }

    @Override
    protected String featureValueOf(DeniedByIpAccessListLine actual) {
      return actual.getName();
    }
  }

  static final class IsDeniedByIpAccessListLineThat
      extends IsInstanceThat<TraceEvent, DeniedByIpAccessListLine> {
    IsDeniedByIpAccessListLineThat(Matcher<? super DeniedByIpAccessListLine> subMatcher) {
      super(DeniedByIpAccessListLine.class, subMatcher);
    }
  }
}
