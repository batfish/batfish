package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.IpSpace;
import org.batfish.common.ip.Prefix;

/** Represents a network object that contains a subnet */
@ParametersAreNonnullByDefault
public final class SubnetNetworkObject implements Serializable, NetworkObject {

  private NetworkObjectInfo _info;

  @Nonnull private final Prefix _subnet;

  public SubnetNetworkObject(Prefix subnet) {
    _subnet = subnet;
  }

  @Override
  public String getDescription() {
    return _info.getDescription();
  }

  @Override
  public Ip getEnd() {
    return _subnet.getEndIp();
  }

  @Override
  public String getName() {
    return _info.getName();
  }

  public Prefix getPrefix() {
    return _subnet;
  }

  @Override
  public Ip getStart() {
    return _subnet.getStartIp();
  }

  @Override
  public void setInfo(NetworkObjectInfo info) {
    _info = info;
  }

  @Override
  public IpSpace toIpSpace() {
    return _subnet.toIpSpace();
  }
}
