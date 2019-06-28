package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilySettings;
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
      extends FeatureMatcher<AddressFamily, AddressFamilySettings> {
    HasAddressFamilySettings(@Nonnull Matcher<? super AddressFamilySettings> subMatcher) {
      super(subMatcher, "An AddressFamily with addressFamilySettings:", "addressFamilySettings");
    }

    @Override
    protected AddressFamilySettings featureValueOf(AddressFamily actual) {
      return actual.getAddressFamilySettings();
    }
  }

  private AddressFamilyMatchersImpl() {}
}
