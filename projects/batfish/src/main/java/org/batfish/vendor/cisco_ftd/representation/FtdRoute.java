package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

public class FtdRoute implements Serializable {

  private final @Nonnull String _interfaceName;
  private final @Nonnull Ip _network;
  private final @Nonnull Ip _mask;
  private final @Nonnull Ip _gateway;
  private final int _metric;

  public FtdRoute(String iface, Ip network, Ip mask, Ip gateway, int metric) {
    _interfaceName = iface;
    _network = network;
    _mask = mask;
    _gateway = gateway;
    _metric = metric;
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nonnull Ip getNetwork() {
    return _network;
  }

  public @Nonnull Ip getMask() {
    return _mask;
  }

  public @Nonnull Ip getGateway() {
    return _gateway;
  }

  public int getMetric() {
    return _metric;
  }
}
