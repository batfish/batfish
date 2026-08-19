package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Vendor-specific representation of an Aruba AOS-CX OSPFv2 process. */
public final class AosCxOspfProcess implements Serializable {

  public AosCxOspfProcess(int processId) {
    _processId = processId;
  }

  public int getProcessId() {
    return _processId;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  private final int _processId;
  private @Nullable Ip _routerId;
}
