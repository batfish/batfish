package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

public class FtdOspfNetwork implements Serializable {

  private final @Nonnull Ip _ip;
  private final @Nonnull Ip _mask; // Wildcard mask in IOS, likely same here
  private final long _areaId;

  public FtdOspfNetwork(Ip ip, Ip mask, long areaId) {
    _ip = ip;
    _mask = mask;
    _areaId = areaId;
  }

  public Ip getIp() {
    return _ip;
  }

  public Ip getMask() {
    return _mask;
  }

  public long getAreaId() {
    return _areaId;
  }
}
