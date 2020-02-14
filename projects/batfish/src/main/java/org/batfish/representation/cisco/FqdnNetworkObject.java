package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.ip.EmptyIpSpace;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.IpSpace;

/** Represents a network object that contains an FQDN */
@ParametersAreNonnullByDefault
public final class FqdnNetworkObject implements Serializable, NetworkObject {

  private NetworkObjectInfo _info;

  @Override
  public String getDescription() {
    return _info.getDescription();
  }

  @Override
  public Ip getEnd() {
    return null;
  }

  @Override
  public String getName() {
    return _info.getName();
  }

  @Override
  public Ip getStart() {
    return null;
  }

  @Override
  public void setInfo(NetworkObjectInfo info) {
    _info = info;
  }

  @Override
  public IpSpace toIpSpace() {
    return EmptyIpSpace.INSTANCE;
  }
}
