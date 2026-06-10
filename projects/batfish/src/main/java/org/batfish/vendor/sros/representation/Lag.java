package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An SR-OS {@code lag "<name>"} (Link Aggregation Group). The LAG name matches {@code lag-.+} (e.g.
 * {@code lag-1}) and a router interface binds it as a port. Models the subset that affects
 * conversion: the LAG name, its admin-state, and the member port names — the LAG interface's
 * bandwidth is the sum of its members' bandwidths.
 */
public final class Lag implements Serializable {

  public Lag(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Whether the LAG is {@code admin-state enable}; defaults true when absent. */
  public boolean getAdminStateEnable() {
    return _adminStateEnable;
  }

  public void setAdminStateEnable(boolean adminStateEnable) {
    _adminStateEnable = adminStateEnable;
  }

  /** The member port paths (e.g. {@code 1/1/c1/1}), in configuration order. */
  public @Nonnull List<String> getMemberPorts() {
    return _memberPorts;
  }

  private final @Nonnull String _name;
  private boolean _adminStateEnable = true;
  private final @Nonnull List<String> _memberPorts = new ArrayList<>();
}
