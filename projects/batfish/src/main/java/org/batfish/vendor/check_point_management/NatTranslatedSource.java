package org.batfish.vendor.check_point_management;

/** Types that can be used as the referent of {@link NatRule#getTranslatedSource()}. */
public interface NatTranslatedSource {
  <T> T accept(NatTranslatedSourceVisitor<T> visitor);
}
