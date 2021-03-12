package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

/** Represents a network object that contains a host */
@ParametersAreNonnullByDefault
public final class HostNetworkObject implements Serializable, NetworkObject {

  @Nonnull private final Ip _host;
  private NetworkObjectInfo _info;

  public HostNetworkObject(Ip host) {
    _host = host;
  }

  @Override
  public String getDescription() {
    return _info.getDescription();
  }

  @Override
  public Ip getEnd() {
    return _host;
  }

  @Override
  public String getName() {
    return _info.getName();
  }

  public Prefix getPrefix() {
    return _host.toPrefix();
  }

  @Override
  public Ip getStart() {
    return _host;
  }

  @Override
  public void setInfo(NetworkObjectInfo info) {
    _info = info;
  }

  @Override
  public IpSpace toIpSpace() {
    return _host.toIpSpace();
  }
}
