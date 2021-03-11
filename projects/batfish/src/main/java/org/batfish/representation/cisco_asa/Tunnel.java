package org.batfish.representation.cisco_asa;

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

  @Nullable private Ip _destination;

  @Nullable private String _ipsecProfileName;

  @Nonnull private TunnelMode _mode;

  @Nullable private Ip _sourceAddress;

  @Nonnull private String _sourceInterfaceName;

  public Tunnel() {
    _sourceInterfaceName = UNSET_LOCAL_INTERFACE;
    _mode = TunnelMode.GRE_MULTIPOINT;
  }

  @Nullable
  public Ip getDestination() {
    return _destination;
  }

  @Nullable
  public String getIpsecProfileName() {
    return _ipsecProfileName;
  }

  @Nonnull
  public TunnelMode getMode() {
    return _mode;
  }

  @Nullable
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @Nonnull
  public String getSourceInterfaceName() {
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
