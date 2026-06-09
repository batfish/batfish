package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * An SR-OS {@code router "<name>" ospf <instance>} process, keyed by its integer instance. Holds
 * the router-id and the areas; the OSPF-enabled interfaces hang off each {@link OspfArea}. Only the
 * subset needed to form adjacencies and compute intra-area routes is modeled.
 */
public final class OspfProcess implements Serializable {

  public OspfProcess(int instance) {
    _instance = instance;
    _areas = new LinkedHashMap<>();
  }

  public int getInstance() {
    return _instance;
  }

  /** The {@code router-id}, or {@code null} if unset (then derived from the system interface). */
  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  /**
   * Whether the process is {@code admin-state enable}; defaults true when admin-state is absent.
   */
  public boolean getAdminStateEnable() {
    return _adminStateEnable;
  }

  public void setAdminStateEnable(boolean adminStateEnable) {
    _adminStateEnable = adminStateEnable;
  }

  /** OSPF areas, keyed by area-id (a dotted-quad string as configured, e.g. {@code 0.0.0.0}). */
  public @Nonnull Map<String, OspfArea> getAreas() {
    return _areas;
  }

  private final int _instance;
  private @Nullable Ip _routerId;
  private boolean _adminStateEnable = true;
  private final @Nonnull Map<String, OspfArea> _areas;
}
