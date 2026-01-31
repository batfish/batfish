package org.batfish.vendor.arista.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

@ParametersAreNonnullByDefault
public final class Tunnel implements Serializable {

  public enum TunnelMode {
    GRE_MULTIPOINT,
    IPSEC_IPV4,
    IPV6_IP,
  }

  private @Nullable Ip _destination;

  private @Nullable String _ipsecProfileName;

  private @Nonnull TunnelMode _mode;

  private @Nullable Ip _sourceAddress;

  private @Nonnull String _sourceInterfaceName;

  public Tunnel() {
    _sourceInterfaceName = UNSET_LOCAL_INTERFACE;
    _mode = TunnelMode.GRE_MULTIPOINT;
  }

  public @Nullable Ip getDestination() {
    return _destination;
  }

  public @Nullable String getIpsecProfileName() {
    return _ipsecProfileName;
  }

  public @Nonnull TunnelMode getMode() {
    return _mode;
  }

  public @Nullable Ip getSourceAddress() {
    return _sourceAddress;
  }

  public @Nonnull String getSourceInterfaceName() {
    return _sourceInterfaceName;
  }

  public void setDestination(@Nullable Ip destination) {
    _destination = destination;
  }

  public void setIpsecProfileName(@Nullable String name) {
    _ipsecProfileName = name;
  }

  public void setMode(TunnelMode mode) {
    _mode = mode;
  }

  public void setSourceAddress(@Nullable Ip source) {
    _sourceAddress = source;
  }

  public void setSourceInterfaceName(@Nullable String sourceInterfaceName) {
    _sourceInterfaceName = firstNonNull(sourceInterfaceName, UNSET_LOCAL_INTERFACE);
  }
}
