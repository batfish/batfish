package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public final class DynamicTunnel implements Serializable {

  public enum Type {
    IPIP,
    UDP
  }

  private boolean _bgpSignal;
  private final @Nonnull SortedMap<Prefix, DynamicTunnelDestination> _destinationNetworks;
  private @Nullable Ip _sourceAddress;
  private @Nullable Type _type;

  public DynamicTunnel() {
    _destinationNetworks = new TreeMap<>();
  }

  public boolean getBgpSignal() {
    return _bgpSignal;
  }

  public @Nonnull SortedMap<Prefix, DynamicTunnelDestination> getDestinationNetworks() {
    return _destinationNetworks;
  }

  public @Nullable Ip getSourceAddress() {
    return _sourceAddress;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public void setBgpSignal(boolean bgpSignal) {
    _bgpSignal = bgpSignal;
  }

  public void setSourceAddress(Ip sourceAddress) {
    _sourceAddress = sourceAddress;
  }

  public void setType(Type type) {
    _type = type;
  }
}
