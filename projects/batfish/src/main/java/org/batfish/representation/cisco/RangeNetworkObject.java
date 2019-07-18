package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;

/** Represents a network object that contains an range */
@ParametersAreNonnullByDefault
public final class RangeNetworkObject implements Serializable, NetworkObject {

  private NetworkObjectInfo _info;
  @Nonnull private final Ip _end;
  @Nonnull private final Ip _start;

  public RangeNetworkObject(Ip start, Ip end) {
    _end = end;
    _start = start;
  }

  @Override
  public String getDescription() {
    return _info.getDescription();
  }

  @Override
  public Ip getEnd() {
    return _end;
  }

  @Override
  public String getName() {
    return _info.getName();
  }

  @Override
  public Ip getStart() {
    return _start;
  }

  @Override
  public void setInfo(NetworkObjectInfo info) {
    _info = info;
  }

  @Override
  public IpSpace toIpSpace() {
    return IpRange.range(_start, _end);
  }
}
