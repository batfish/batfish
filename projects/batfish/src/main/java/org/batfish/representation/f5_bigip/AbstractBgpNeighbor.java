package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a BGP neighbor. */
@ParametersAreNonnullByDefault
public abstract class AbstractBgpNeighbor implements Serializable {

  private @Nullable String _description;
  private final @Nonnull BgpNeighborIpv4AddressFamily _ipv4AddressFamily;
  private final @Nonnull BgpNeighborIpv6AddressFamily _ipv6AddressFamily;
  private final @Nonnull String _name;
  private @Nullable Boolean _nextHopSelf;
  private @Nullable Long _remoteAs;
  private @Nullable UpdateSource _updateSource;

  public AbstractBgpNeighbor(String name) {
    _name = name;
    _ipv4AddressFamily = new BgpNeighborIpv4AddressFamily();
    _ipv6AddressFamily = new BgpNeighborIpv6AddressFamily();
  }

  public final @Nullable String getDescription() {
    return _description;
  }

  public final @Nonnull BgpNeighborIpv4AddressFamily getIpv4AddressFamily() {
    return _ipv4AddressFamily;
  }

  public final @Nonnull BgpNeighborIpv6AddressFamily getIpv6AddressFamily() {
    return _ipv6AddressFamily;
  }

  public final @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public final @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public final @Nullable UpdateSource getUpdateSource() {
    return _updateSource;
  }

  public final void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setNextHopSelf(@Nullable Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  public final void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public final void setUpdateSource(@Nullable UpdateSource updateSource) {
    _updateSource = updateSource;
  }
}
