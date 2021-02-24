package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** OSPF configuration for a particular VRF. */
public class OspfVrf implements Serializable {

  private final @Nonnull String _vrfName;
  private @Nullable Ip _routerId;
  private final @Nonnull Map<Long, OspfArea> _areas;

  public OspfVrf(String name) {
    _vrfName = name;
    _areas = new HashMap<>();
  }

  public @Nonnull OspfArea getOrCreateArea(long area) {
    return _areas.computeIfAbsent(area, OspfArea::new);
  }

  public @Nullable OspfArea getArea(long area) {
    return _areas.get(area);
  }

  public @Nonnull Map<Long, OspfArea> getAreas() {
    return Collections.unmodifiableMap(_areas);
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }
}
