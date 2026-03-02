package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;

/**
 * OSPF configuration for L3Out.
 *
 * <p>Defines OSPF process settings, areas, and interfaces for an L3Out.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OspfConfig implements Serializable {
  private String _name;
  private String _description;
  private String _processId;
  private String _areaId;
  private Map<String, OspfArea> _areas;
  private List<OspfInterface> _ospfInterfaces;

  public OspfConfig() {
    _areas = new TreeMap<>();
    _ospfInterfaces = new ArrayList<>();
  }

  public @Nullable String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable String getProcessId() {
    return _processId;
  }

  public void setProcessId(String processId) {
    _processId = processId;
  }

  public @Nullable String getAreaId() {
    return _areaId;
  }

  public void setAreaId(String areaId) {
    _areaId = areaId;
  }

  public Map<String, OspfArea> getAreas() {
    return _areas;
  }

  public void setAreas(Map<String, OspfArea> areas) {
    _areas = new TreeMap<>(areas);
  }

  public List<OspfInterface> getOspfInterfaces() {
    return _ospfInterfaces;
  }

  public void setOspfInterfaces(List<OspfInterface> ospfInterfaces) {
    _ospfInterfaces = new ArrayList<>(ospfInterfaces);
  }
}
