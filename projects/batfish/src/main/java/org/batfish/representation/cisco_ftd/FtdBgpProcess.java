package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class FtdBgpProcess implements Serializable {

  public FtdBgpProcess(long asn) {
    _asn = asn;
    _neighbors = new HashMap<>();
  }

  public long getAsn() {
    return _asn;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nonnull Map<Ip, FtdBgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public boolean hasIpv4AddressFamily() {
    return _hasIpv4AddressFamily;
  }

  public void setHasIpv4AddressFamily(boolean hasIpv4AddressFamily) {
    _hasIpv4AddressFamily = hasIpv4AddressFamily;
  }

  private final long _asn;
  private @Nullable Ip _routerId;
  private final Map<Ip, FtdBgpNeighbor> _neighbors;
  private boolean _hasIpv4AddressFamily;
}
