package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Vendor-specific representation of an Aruba AOS-CX OSPFv3 process. */
public final class AosCxOspfv3Process implements Serializable {

  public AosCxOspfv3Process(int processId) {
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

  public @Nonnull Set<String> getAreas() {
    return _areas;
  }

  public void addArea(String area) {
    _areas.add(area);
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  private final int _processId;
  private boolean _redistributeConnected;
  private final @Nonnull Set<String> _areas = new HashSet<>();
  private @Nullable Ip _routerId;
}
