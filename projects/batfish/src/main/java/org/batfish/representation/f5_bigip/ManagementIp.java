package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;

/** A reference to the management IP of a {@link Device} within a {@link UnicastAddress}. */
public class ManagementIp implements UnicastAddressIp {

  public static @Nonnull ManagementIp instance() {
    return INSTANCE;
  }

  private static final ManagementIp INSTANCE = new ManagementIp();

  private ManagementIp() {}
}
