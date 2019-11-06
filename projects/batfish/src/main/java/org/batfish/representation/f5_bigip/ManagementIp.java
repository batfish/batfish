package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;

/** A reference to the management IP of a {@link Device} within a {@link UnicastAddress}. */
public class ManagementIp implements UnicastAddressIp {

  public static @Nonnull ManagementIp instance() {
    return INSTANCE;
  }

  @Override
  public @Nonnull org.batfish.datamodel.vendor_family.f5_bigip.ManagementIp toUnicastAddressIp() {
    return org.batfish.datamodel.vendor_family.f5_bigip.ManagementIp.instance();
  }

  private static final ManagementIp INSTANCE = new ManagementIp();

  private ManagementIp() {}
}
