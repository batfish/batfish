package org.batfish.datamodel.vendor_family.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A reference to the management IP of a {@link Device} within a {@link UnicastAddress}. */
public class ManagementIp implements UnicastAddressIp {

  public static @Nonnull ManagementIp instance() {
    return INSTANCE;
  }

  private static final ManagementIp INSTANCE = new ManagementIp();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof ManagementIp;
  }

  @Override
  public int hashCode() {
    return 0x319397A0; // randomly generate
  }

  private ManagementIp() {}
}
