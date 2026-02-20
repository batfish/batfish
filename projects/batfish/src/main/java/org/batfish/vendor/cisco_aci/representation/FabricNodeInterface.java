package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Interface configuration on a fabric node.
 *
 * <p>Extracted from {@code FabricNode.Interface} to avoid Java reserved-word conflict and to align
 * with the top-level file pattern.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FabricNodeInterface implements Serializable {
  private String _name;
  private String _type;
  private String _description;
  private boolean _enabled;
  private String _epg;
  private String _vlan;

  public FabricNodeInterface() {
    _enabled = true;
  }

  public @Nullable String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public @Nullable String getType() {
    return _type;
  }

  public void setType(String type) {
    _type = type;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public boolean isEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public @Nullable String getEpg() {
    return _epg;
  }

  public void setEpg(String epg) {
    _epg = epg;
  }

  public @Nullable String getVlan() {
    return _vlan;
  }

  public void setVlan(String vlan) {
    _vlan = vlan;
  }
}
