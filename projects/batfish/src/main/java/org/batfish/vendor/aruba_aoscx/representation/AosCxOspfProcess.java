package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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

  public boolean getRedistributeConnected() {
    return _redistributeConnected;
  }

  public void setRedistributeConnected(boolean redistributeConnected) {
    _redistributeConnected = redistributeConnected;
  }

  public Map<String, Boolean> getStubAreas() {
    return _stubAreas;
  }

  public void setStubArea(String areaId, boolean suppressType3) {
    _stubAreas.put(areaId, suppressType3);
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  private final int _processId;
  private boolean _redistributeConnected;
  private final Map<String, Boolean> _stubAreas = new HashMap<>();
  private @Nullable Ip _routerId;
}
