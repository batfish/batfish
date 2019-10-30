package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** A concrete or reference IP assigned to a {@link Device} within a {@link UnicastAddress}. */
public interface UnicastAddressIp extends Serializable {

  /** Convert to vendor-independent form. */
  // TODO: Just resolve IPs and eliminate VI UnicastAddressIp
  @Nonnull
  org.batfish.datamodel.vendor_family.f5_bigip.UnicastAddressIp toUnicastAddressIp();
}
