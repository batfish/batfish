package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class AddressFamilyMatchersImpl {
  static final class HasExportPolicy extends FeatureMatcher<AddressFamily, String> {
    HasExportPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AddressFamily with exportPolicy:", "exportPolicy");
    }

    @Override
    protected String featureValueOf(AddressFamily actual) {
      return actual.getExportPolicy();
    }
  }

  static final class HasImportPolicy extends FeatureMatcher<AddressFamily, String> {
    HasImportPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AddressFamily with importPolicy:", "importPolicy");
    }

    @Override
    protected String featureValueOf(AddressFamily actual) {
      return actual.getImportPolicy();
    }
  }

  static final class HasAddressFamilyCapabilities
      extends FeatureMatcher<AddressFamily, AddressFamilyCapabilities> {
    HasAddressFamilyCapabilities(@Nonnull Matcher<? super AddressFamilyCapabilities> subMatcher) {
      super(subMatcher, "An AddressFamily with addressFamilySettings:", "addressFamilySettings");
    }

    @Override
    protected AddressFamilyCapabilities featureValueOf(AddressFamily actual) {
      return actual.getAddressFamilyCapabilities();
    }
  }

  static final class HasRouteReflectorClient extends FeatureMatcher<AddressFamily, Boolean> {
    HasRouteReflectorClient(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamily with routeReflectorClient:", "routeReflectorClient");
    }

    @Override
    protected Boolean featureValueOf(AddressFamily actual) {
      return actual.getRouteReflectorClient();
    }
  }

  private AddressFamilyMatchersImpl() {}
}
