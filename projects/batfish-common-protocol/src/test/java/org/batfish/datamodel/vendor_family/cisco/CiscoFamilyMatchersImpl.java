package org.batfish.datamodel.vendor_family.cisco;

import javax.annotation.Nonnull;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class CiscoFamilyMatchersImpl {

  static final class HasLogging extends FeatureMatcher<CiscoFamily, Logging> {
    HasLogging(@Nonnull Matcher<? super Logging> subMatcher) {
      super(subMatcher, "a CiscoFamily with logging", "logging");
    }

    @Override
    protected Logging featureValueOf(CiscoFamily actual) {
      return actual.getLogging();
    }
  }

  static final class HasAaa extends FeatureMatcher<CiscoFamily, Aaa> {
    HasAaa(@Nonnull Matcher<? super Aaa> subMatcher) {
      super(subMatcher, "a CiscoFamily with aaa", "aaa");
    }

    @Override
    protected Aaa featureValueOf(CiscoFamily actual) {
      return actual.getAaa();
    }
  }

  private CiscoFamilyMatchersImpl() {}
}
