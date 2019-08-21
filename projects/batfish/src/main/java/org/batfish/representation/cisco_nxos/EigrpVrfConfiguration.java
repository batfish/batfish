package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Represents the VRF-specific EIGRP configuration for an EIGRP process in Cisco NX-OS.
 *
 * <p>Configuration commands entered at the {@code config-router} level that can also be run at the
 * {@code config-router-vrf} level.
 */
public final class EigrpVrfConfiguration implements Serializable {

  public EigrpVrfConfiguration() {}

  public @Nullable Integer getAsn() {
    return _asn;
  }

  public void setAsn(@Nullable Integer asn) {
    _asn = asn;
  }

  ///////////////////////////////////
  // Private implementation details
  ///////////////////////////////////

  private @Nullable Integer _asn;
}
