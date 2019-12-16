package org.batfish.datamodel.matchers;

import org.batfish.datamodel.acl.PermittedByAclLine;
import org.batfish.datamodel.acl.TraceEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class PermittedByAclLineMatchersImpl {
  static final class HasIndex extends FeatureMatcher<PermittedByAclLine, Integer> {
    HasIndex(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "a PermittedByAclLine with index:", "index");
    }

    @Override
    protected Integer featureValueOf(PermittedByAclLine actual) {
      return actual.getIndex();
    }
  }

  static final class HasLineDescription extends FeatureMatcher<PermittedByAclLine, String> {
    public HasLineDescription(Matcher<? super String> subMatcher) {
      super(subMatcher, "a PermittedByAclLine with lineDescription:", "lineDescription");
    }

    @Override
    protected String featureValueOf(PermittedByAclLine actual) {
      return actual.getLineDescription();
    }
  }

  static final class HasName extends FeatureMatcher<PermittedByAclLine, String> {
    public HasName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a PermittedByAclLine with name:", "name");
    }

    @Override
    protected String featureValueOf(PermittedByAclLine actual) {
      return actual.getName();
    }
  }

  static final class IsPermittedByAclLineThat
      extends IsInstanceThat<TraceEvent, PermittedByAclLine> {
    IsPermittedByAclLineThat(Matcher<? super PermittedByAclLine> subMatcher) {
      super(PermittedByAclLine.class, subMatcher);
    }
  }
}
