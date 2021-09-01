package org.batfish.vendor.check_point_management;

/**
 * Types that can be used as a {@link NatRule#getTranslatedSource()} () NAT rule's translated
 * source}
 */
public interface NatTranslatedSrcOrDst {
  <T> T accept(NatTranslatedSrcOrDstVisitor<T> visitor);
}
