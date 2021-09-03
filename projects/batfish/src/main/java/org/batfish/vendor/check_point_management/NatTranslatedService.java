package org.batfish.vendor.check_point_management;

/**
 * Types that can be used as a {@link NatRule#getTranslatedService() NAT rule's translated service}
 */
public interface NatTranslatedService {
  <T> T accept(NatTranslatedServiceVisitor<T> visitor);
}
