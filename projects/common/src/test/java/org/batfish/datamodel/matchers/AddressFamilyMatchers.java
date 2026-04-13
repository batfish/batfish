package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link AddressFamily} */
public final class AddressFamilyMatchers {
  /**
   * Provides a matcher that matches if the {@link AddressFamily} has the specified {@code
   * addressFamilyCapabilities}.
   */
  public static @Nonnull Matcher<AddressFamily> hasAddressFamilyCapabilites(
      Matcher<? super AddressFamilyCapabilities> addressFamilyCapabilities) {
    return new HasAddressFamilyCapabilities(addressFamilyCapabilities);
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamily} has the specified {@code
   * expectedExportPolicy}.
   */
  public static @Nonnull Matcher<AddressFamily> hasExportPolicy(String expectedExportPolicy) {
    return new HasExportPolicy(equalTo(expectedExportPolicy));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamily} has the specified {@code
   * expectedImportPolicy}.
   */
  public static @Nonnull Matcher<AddressFamily> hasImportPolicy(String expectedImportPolicy) {
    return new HasImportPolicy(equalTo(expectedImportPolicy));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamily} has the specified {@code
   * routeReflectorClient}.
   */
  public static @Nonnull Matcher<AddressFamily> hasRouteReflectorClient(
      boolean routeReflectorClient) {
    return new HasRouteReflectorClient(equalTo(routeReflectorClient));
  }

  private AddressFamilyMatchers() {}

  private static final class HasExportPolicy extends FeatureMatcher<AddressFamily, String> {
    HasExportPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AddressFamily with exportPolicy:", "exportPolicy");
    }

    @Override
    protected String featureValueOf(AddressFamily actual) {
      return actual.getExportPolicy();
    }
  }

  private static final class HasImportPolicy extends FeatureMatcher<AddressFamily, String> {
    HasImportPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AddressFamily with importPolicy:", "importPolicy");
    }

    @Override
    protected String featureValueOf(AddressFamily actual) {
      return actual.getImportPolicy();
    }
  }

  private static final class HasAddressFamilyCapabilities
      extends FeatureMatcher<AddressFamily, AddressFamilyCapabilities> {
    HasAddressFamilyCapabilities(@Nonnull Matcher<? super AddressFamilyCapabilities> subMatcher) {
      super(subMatcher, "An AddressFamily with addressFamilySettings:", "addressFamilySettings");
    }

    @Override
    protected AddressFamilyCapabilities featureValueOf(AddressFamily actual) {
      return actual.getAddressFamilyCapabilities();
    }
  }

  private static final class HasRouteReflectorClient
      extends FeatureMatcher<AddressFamily, Boolean> {
    HasRouteReflectorClient(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamily with routeReflectorClient:", "routeReflectorClient");
    }

    @Override
    protected Boolean featureValueOf(AddressFamily actual) {
      return actual.getRouteReflectorClient();
    }
  }
}
