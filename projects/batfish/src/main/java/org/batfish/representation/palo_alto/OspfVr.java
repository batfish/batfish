package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Configuration of OSPF within a virtual-router. Config at {@code network virtual-router NAME
 * protocol ospf}.
 */
public class OspfVr implements Serializable {
  public static final int DEFAULT_LOOPBACK_OSPF_COST = 1;

  /** From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_ENABLE = false;
  /** From PAN admin UI - only shows in running config if checked (as no). */
  private static final boolean DEFAULT_REJECT_DEFAULT_ROUTE = true;

  public OspfVr() {
    _areas = new HashMap<>();
    _enable = DEFAULT_ENABLE;
    _rejectDefaultRoute = DEFAULT_REJECT_DEFAULT_ROUTE;
  }

  public @Nonnull OspfArea getOrCreateOspfArea(Ip areaId) {
    return _areas.computeIfAbsent(areaId, OspfArea::new);
  }

  @Nonnull
  public Map<Ip, OspfArea> getAreas() {
    return _areas;
  }

  public boolean isEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  /**
   * If set then no default routes will be learnt through OSPF
   *
   * @return true if no default routes will be learnt through OSPF
   */
  public boolean isRejectDefaultRoute() {
    return _rejectDefaultRoute;
  }

  public void setRejectDefaultRoute(boolean rejectDefaultRoute) {
    _rejectDefaultRoute = rejectDefaultRoute;
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////
  private Map<Ip, OspfArea> _areas;
  private boolean _enable;
  private boolean _rejectDefaultRoute;
  private @Nullable Ip _routerId;
}
