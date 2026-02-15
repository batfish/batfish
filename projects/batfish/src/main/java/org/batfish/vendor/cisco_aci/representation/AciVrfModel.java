package org.batfish.vendor.cisco_aci.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * ACI VRF configuration model class.
 *
 * <p>A VRF (Virtual Routing and Forwarding) instance provides Layer 3 routing isolation within a
 * tenant.
 */
public class AciVrfModel implements Serializable {
  private final String _name;
  private @Nullable String _tenant;
  private @Nullable String _description;

  public AciVrfModel(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getTenant() {
    return _tenant;
  }

  public void setTenant(@Nullable String tenant) {
    _tenant = tenant;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }
}
