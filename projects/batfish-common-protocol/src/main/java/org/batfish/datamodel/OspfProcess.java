package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;

@JsonSchemaDescription("An OSPF routing process")
public class OspfProcess implements Serializable {

  private static final int DEFAULT_CISCO_VLAN_OSPF_COST = 1;

  private static final long serialVersionUID = 1L;

  private SortedMap<Long, OspfArea> _areas;

  private String _exportPolicy;

  private SortedSet<GeneratedRoute> _generatedRoutes;

  private transient Map<Pair<Ip, Ip>, OspfNeighbor> _ospfNeighbors;

  private Double _referenceBandwidth;

  private @Nullable Boolean _rfc1583Compatible;

  private Ip _routerId;

  public OspfProcess() {
    _generatedRoutes = new TreeSet<>();
    _areas = new TreeMap<>();
  }

  @JsonPropertyDescription("The OSPF areas contained in this process")
  public SortedMap<Long, OspfArea> getAreas() {
    return _areas;
  }

  @JsonPropertyDescription(
      "The routing policy applied to routes in the main RIB to determine which ones are to be "
          + "exported into OSPF and how")
  public String getExportPolicy() {
    return _exportPolicy;
  }

  @JsonPropertyDescription(
      "Generated IPV4 routes for the purpose of export into OSPF. These routes are not imported "
          + "into the main RIB.")
  public SortedSet<GeneratedRoute> getGeneratedRoutes() {
    return _generatedRoutes;
  }

  @JsonIgnore
  public Map<Pair<Ip, Ip>, OspfNeighbor> getOspfNeighbors() {
    return _ospfNeighbors;
  }

  @JsonPropertyDescription(
      "The reference bandwidth by which an interface's bandwidth is divided to determine its OSPF "
          + "cost")
  public Double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public @Nullable Boolean getRfc1583Compatible() {
    return _rfc1583Compatible;
  }

  @JsonPropertyDescription("The router-id of this OSPF process")
  public Ip getRouterId() {
    return _routerId;
  }

  public void initInterfaceCosts() {
    for (OspfArea area : _areas.values()) {
      for (Interface i : area.getInterfaces()) {
        String interfaceName = i.getName();
        if (i.getActive()) {
          Integer ospfCost = i.getOspfCost();
          if (ospfCost == null) {
            if (interfaceName.startsWith("Vlan")) {
              // TODO: fix for non-cisco
              ospfCost = DEFAULT_CISCO_VLAN_OSPF_COST;
            } else {
              if (i.getBandwidth() != null) {
                ospfCost = Math.max((int) (_referenceBandwidth / i.getBandwidth()), 1);
              } else {
                String hostname = i.getOwner().getHostname();
                throw new BatfishException(
                    "Expected non-null interface bandwidth for \""
                        + hostname
                        + "\":\""
                        + interfaceName
                        + "\"");
              }
            }
          }
          i.setOspfCost(ospfCost);
        }
      }
    }
  }

  public void setAreas(SortedMap<Long, OspfArea> areas) {
    _areas = areas;
  }

  public void setExportPolicy(String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  public void setGeneratedRoutes(SortedSet<GeneratedRoute> generatedRoutes) {
    _generatedRoutes = generatedRoutes;
  }

  @JsonIgnore
  public void setOspfNeighbors(Map<Pair<Ip, Ip>, OspfNeighbor> ospfNeighbors) {
    _ospfNeighbors = ospfNeighbors;
  }

  public void setReferenceBandwidth(Double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  public void setRfc1583Compatible(Boolean rfc1583Compatible) {
    _rfc1583Compatible = rfc1583Compatible;
  }

  public void setRouterId(Ip id) {
    _routerId = id;
  }
}
