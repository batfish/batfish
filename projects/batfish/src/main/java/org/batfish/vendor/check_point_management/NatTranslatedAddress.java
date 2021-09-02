package org.batfish.vendor.check_point_management;

/**
 * Types that can be used as the referent of {@link NatRule#getTranslatedSource()} or {@link
 * NatRule#getTranslatedDestination()} ()}.
 */
public interface NatTranslatedAddress {
  <T> T accept(NatTranslatedAddressVisitor<T> visitor);
}
