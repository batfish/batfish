package org.batfish.vendor.check_point_management;

/** Types that can be used as the referent of {@link NatRule#getTranslatedDestination()}. */
public interface NatTranslatedDestination {
  <T> T accept(NatTranslatedDestinationVisitor<T> visitor);
}
