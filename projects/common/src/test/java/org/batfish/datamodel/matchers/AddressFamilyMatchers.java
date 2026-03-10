package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.matchers.AddressFamilyMatchersImpl.HasAddressFamilyCapabilities;
import org.batfish.datamodel.matchers.AddressFamilyMatchersImpl.HasExportPolicy;
import org.batfish.datamodel.matchers.AddressFamilyMatchersImpl.HasImportPolicy;
import org.batfish.datamodel.matchers.AddressFamilyMatchersImpl.HasRouteReflectorClient;
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
}
