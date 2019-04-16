package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** BGP process configuration */
@ParametersAreNonnullByDefault
public final class BgpProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _alwaysCompareMed;
  private boolean _deterministicMed;
  private final @Nonnull BgpIpv4AddressFamily _ipv4AddressFamily;
  private final @Nonnull BgpIpv6AddressFamily _ipv6AddressFamily;
  private @Nullable Long _localAs;
  private final @Nonnull String _name;
  private final @Nonnull Map<String, BgpNeighbor> _neighbors;
  private final @Nonnull Map<String, BgpPeerGroup> _peerGroups;
  private @Nullable Ip _routerId;

  public BgpProcess(String name) {
    _name = name;
    _neighbors = new HashMap<>();
    _ipv4AddressFamily = new BgpIpv4AddressFamily();
    _ipv6AddressFamily = new BgpIpv6AddressFamily();
    _peerGroups = new HashMap<>();
  }

  public boolean getAlwaysCompareMed() {
    return _alwaysCompareMed;
  }

  public boolean getDeterministicMed() {
    return _deterministicMed;
  }

  public @Nonnull BgpIpv4AddressFamily getIpv4AddressFamily() {
    return _ipv4AddressFamily;
  }

  public @Nonnull BgpIpv6AddressFamily getIpv6AddressFamily() {
    return _ipv6AddressFamily;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<String, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  public @Nonnull Map<String, BgpPeerGroup> getPeerGroups() {
    return _peerGroups;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setAlwaysCompareMed(boolean alwaysCompareMed) {
    _alwaysCompareMed = alwaysCompareMed;
  }

  public void setDeterministicMed(boolean deterministicMed) {
    _deterministicMed = deterministicMed;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }
}
