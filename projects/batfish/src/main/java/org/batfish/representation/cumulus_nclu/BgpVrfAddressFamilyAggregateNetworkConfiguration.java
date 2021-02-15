package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;

/**
 * Represents the BGP configuration for an aggregate network configured for an address family in a
 * VRF.
 *
 * <p>Configured using the {@code aggregate-address} command in {@code /etc/frr/frr.conf}.
 */
public final class BgpVrfAddressFamilyAggregateNetworkConfiguration implements Serializable {
  private boolean _summaryOnly;

  public boolean isSummaryOnly() {
    return _summaryOnly;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }
}
