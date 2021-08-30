package org.batfish.vendor.check_point_management;

/** A concrete service object (does not include {@link CpmiAnyObject}) */
public interface ConcreteService extends Service, NatTranslatedService {
  <T> T accept(ConcreteServiceVisitor<T> visitor);

  default <T> T accept(ServiceVisitor<T> visitor) {
    return accept((ConcreteServiceVisitor<T>) visitor);
  }

  default <T> T accept(NatTranslatedServiceVisitor<T> visitor) {
    return accept((ConcreteServiceVisitor<T>) visitor);
  }
}
