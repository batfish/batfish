package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;

public final class Tunnel implements Serializable {

  public enum TunnelMode {
    GRE,
    IPSEC
  }

  private static final long serialVersionUID = 1L;

  private Ip _destination;

  private String _ipsecProfileName;

  private TunnelMode _mode;

  private IpProtocol _protocol;

  private @Nullable Ip _sourceAddress;

  private @Nonnull String _sourceInterfaceName;

  public Tunnel() {
    _sourceInterfaceName = UNSET_LOCAL_INTERFACE;
  }

  public Ip getDestination() {
    return _destination;
  }

  public String getIpsecProfileName() {
    return _ipsecProfileName;
  }

  public TunnelMode getMode() {
    return _mode;
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }

  @Nullable
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @Nonnull
  public String getSourceInterfaceName() {
    return _sourceInterfaceName;
  }

  public void setDestination(Ip destination) {
    _destination = destination;
  }

  public void setIpsecProfileName(String name) {
    _ipsecProfileName = name;
  }

  public void setMode(TunnelMode mode) {
    _mode = mode;
  }

  public void setProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  public void setSourceAddress(@Nullable Ip source) {
    _sourceAddress = source;
  }

  public void setSourceInterfaceName(@Nullable String sourceInterfaceName) {
    _sourceInterfaceName = firstNonNull(sourceInterfaceName, UNSET_LOCAL_INTERFACE);
  }
}
