package org.batfish.vendor.check_point_management;

/** Types that represent services, including {@link CpmiAnyObject} */
public interface Service extends NatTranslatedService {
  @Override
  default <T> T accept(NatTranslatedServiceVisitor<T> visitor) {
    // TODO: may want to implement solely in implementing classes instead
    return visitor.visitService(this);
  }

  <T> T accept(ServiceVisitor<T> visitor);
}
