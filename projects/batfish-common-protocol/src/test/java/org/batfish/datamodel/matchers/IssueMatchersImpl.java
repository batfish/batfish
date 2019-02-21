package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Issue;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IssueMatchersImpl {

  static final class HasMajorType extends FeatureMatcher<Issue, String> {
    HasMajorType(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Issue with majorType:", "majorType");
    }

    @Override
    protected String featureValueOf(Issue actual) {
      return actual.getType().getMajor();
    }
  }

  static final class HasMinorType extends FeatureMatcher<Issue, String> {
    HasMinorType(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Issue with minorType:", "minorType");
    }

    @Override
    protected String featureValueOf(Issue actual) {
      return actual.getType().getMinor();
    }
  }

  private IssueMatchersImpl() {}
}
