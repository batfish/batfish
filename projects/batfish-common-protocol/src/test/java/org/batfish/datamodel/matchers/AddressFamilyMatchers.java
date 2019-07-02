package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.matchers.AddressFamilyMatchersImpl.HasAddressFamilySettings;
import org.batfish.datamodel.matchers.AddressFamilyMatchersImpl.HasExportPolicy;
import org.hamcrest.Matcher;

/** Matchers for {@link AddressFamily} */
public final class AddressFamilyMatchers {
  /**
   * Provides a matcher that matches if the {@link AddressFamily} has the specified {@code
   * addressFamilySettings}.
   */
  public static @Nonnull Matcher<AddressFamily> hasAddressFamilySettings(
      Matcher<? super AddressFamilyCapabilities> addressFamilySettings) {
    return new HasAddressFamilySettings(addressFamilySettings);
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamily} has the specified {@code
   * expectedExportPolicy}.
   */
  public static @Nonnull Matcher<AddressFamily> hasExportPolicy(String expectedExportPolicy) {
    return new HasExportPolicy(equalTo(expectedExportPolicy));
  }

  private AddressFamilyMatchers() {}
}
