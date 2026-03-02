package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * OSPF area configuration for L3Out.
 *
 * <p>Defines an OSPF area within an L3Out OSPF configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OspfArea implements Serializable {
  private String _areaId;
  private List<String> _networks;
  private String _areaType;

  public OspfArea() {
    _networks = new ArrayList<>();
  }

  public @Nullable String getAreaId() {
    return _areaId;
  }

  public void setAreaId(String areaId) {
    _areaId = areaId;
  }

  public List<String> getNetworks() {
    return _networks;
  }

  public void setNetworks(List<String> networks) {
    _networks = new ArrayList<>(networks);
  }

  public @Nullable String getAreaType() {
    return _areaType;
  }

  public void setAreaType(String areaType) {
    _areaType = areaType;
  }
}
