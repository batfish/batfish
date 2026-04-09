package org.batfish.datamodel.vendor_family.cisco;

import javax.annotation.Nonnull;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class CiscoFamilyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the CiscoFamily's
   * logging.
   */
  public static Matcher<CiscoFamily> hasLogging(Matcher<? super Logging> subMatcher) {
    return new HasLogging(subMatcher);
  }

  public static Matcher<CiscoFamily> hasAaa(Matcher<? super Aaa> subMatcher) {
    return new HasAaa(subMatcher);
  }

  private static final class HasLogging extends FeatureMatcher<CiscoFamily, Logging> {
    HasLogging(@Nonnull Matcher<? super Logging> subMatcher) {
      super(subMatcher, "a CiscoFamily with logging", "logging");
    }

    @Override
    protected Logging featureValueOf(CiscoFamily actual) {
      return actual.getLogging();
    }
  }

  private static final class HasAaa extends FeatureMatcher<CiscoFamily, Aaa> {
    HasAaa(@Nonnull Matcher<? super Aaa> subMatcher) {
      super(subMatcher, "a CiscoFamily with aaa", "aaa");
    }

    @Override
    protected Aaa featureValueOf(CiscoFamily actual) {
      return actual.getAaa();
    }
  }
}
