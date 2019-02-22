package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a BGP neighbor. */
@ParametersAreNonnullByDefault
public final class BgpNeighbor implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;
  private @Nullable String _description;
  private final @Nonnull BgpNeighborIpv4AddressFamily _ipv4AddressFamily;
  private final @Nonnull BgpNeighborIpv6AddressFamily _ipv6AddressFamily;
  private final @Nonnull String _name;
  private @Nullable Long _remoteAs;
  private @Nullable String _updateSource;

  public BgpNeighbor(String name) {
    _name = name;
    _ipv4AddressFamily = new BgpNeighborIpv4AddressFamily();
    _ipv6AddressFamily = new BgpNeighborIpv6AddressFamily();
    Ip.tryParse(_name).ifPresent(this::setAddress);
    Ip6.tryParse(_name).ifPresent(this::setAddress6);
  }

  public @Nullable Ip getAddress() {
    return _address;
  }

  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull BgpNeighborIpv4AddressFamily getIpv4AddressFamily() {
    return _ipv4AddressFamily;
  }

  public @Nonnull BgpNeighborIpv6AddressFamily getIpv6AddressFamily() {
    return _ipv6AddressFamily;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public @Nullable String getUpdateSource() {
    return _updateSource;
  }

  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public void setUpdateSource(@Nullable String updateSource) {
    _updateSource = updateSource;
  }
}
