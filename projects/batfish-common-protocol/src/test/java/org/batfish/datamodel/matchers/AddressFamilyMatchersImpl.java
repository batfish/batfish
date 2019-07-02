package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class AddressFamilyMatchersImpl {
  static final class HasExportPolicy extends FeatureMatcher<AddressFamily, String> {
    HasExportPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A AddressFamily with exportPolicy:", "exportPolicy");
    }

    @Override
    protected String featureValueOf(AddressFamily actual) {
      return actual.getExportPolicy();
    }
  }

  static final class HasAddressFamilySettings
      extends FeatureMatcher<AddressFamily, AddressFamilyCapabilities> {
    HasAddressFamilySettings(@Nonnull Matcher<? super AddressFamilyCapabilities> subMatcher) {
      super(subMatcher, "An AddressFamily with addressFamilySettings:", "addressFamilySettings");
    }

    @Override
    protected AddressFamilyCapabilities featureValueOf(AddressFamily actual) {
      return actual.getAddressFamilyCapabilities();
    }
  }

  private AddressFamilyMatchersImpl() {}
}
