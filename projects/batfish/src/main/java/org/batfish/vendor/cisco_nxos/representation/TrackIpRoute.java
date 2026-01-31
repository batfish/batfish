package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

public final class TrackIpRoute implements Track {

  public TrackIpRoute(Prefix prefix, boolean hmm) {
    _prefix = prefix;
    _hmm = hmm;
  }

  public boolean getHmm() {
    return _hmm;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }

  private final boolean _hmm;
  private final @Nonnull Prefix _prefix;
  private @Nullable String _vrf;
}
