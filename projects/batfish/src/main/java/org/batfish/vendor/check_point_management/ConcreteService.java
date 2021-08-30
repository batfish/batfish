package org.batfish.vendor.check_point_management;

/** A concrete service object (does not include {@link CpmiAnyObject}) */
public interface ConcreteService extends Service {
  <T> T accept(ConcreteServiceVisitor<T> visitor);

  @Override
  default <T> T accept(ServiceVisitor<T> visitor) {
    return accept((ConcreteServiceVisitor<T>) visitor);
  }
}
