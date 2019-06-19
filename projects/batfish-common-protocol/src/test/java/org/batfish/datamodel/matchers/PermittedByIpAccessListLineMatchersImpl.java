package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.PermittedByIpAccessListLine;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class PermittedByIpAccessListLineMatchersImpl {
  static final class HasIndex extends FeatureMatcher<PermittedByIpAccessListLine, Integer> {
    HasIndex(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "a PermittedByIpAccessListLine with index:", "index");
    }

    @Override
    protected Integer featureValueOf(PermittedByIpAccessListLine actual) {
      return actual.getIndex();
    }
  }

  static final class HasLineDescription
      extends FeatureMatcher<PermittedByIpAccessListLine, String> {
    public HasLineDescription(Matcher<? super String> subMatcher) {
      super(subMatcher, "a PermittedByIpAccessListLine with lineDescription:", "lineDescription");
    }

    @Override
    protected String featureValueOf(PermittedByIpAccessListLine actual) {
      return actual.getLineDescription();
    }
  }

  static final class HasName extends FeatureMatcher<PermittedByIpAccessListLine, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a PermittedByIpAccessListLine with name:", "name");
    }

    @Override
    protected String featureValueOf(PermittedByIpAccessListLine actual) {
      return actual.getName();
    }
  }

  static final class IsPermittedByIpAccessListLineThat
      extends IsInstanceThat<TraceEvent, PermittedByIpAccessListLine> {
    IsPermittedByIpAccessListLineThat(Matcher<? super PermittedByIpAccessListLine> subMatcher) {
      super(PermittedByIpAccessListLine.class, subMatcher);
    }
  }
}
