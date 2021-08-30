package org.batfish.vendor.check_point_management;

/** Types that represent services, including {@link CpmiAnyObject} */
public interface Service {
  <T> T accept(ServiceVisitor<T> visitor);
}
