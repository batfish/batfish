package org.batfish.datamodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("An OSPF routing process")
public class OspfProcess implements Serializable {

   private static final long serialVersionUID = 1L;

   private Map<Long, OspfArea> _areas;

   private String _exportPolicy;

   private Set<GeneratedRoute> _generatedRoutes;

   private Double _referenceBandwidth;

   private Ip _routerId;

   public OspfProcess() {
      _generatedRoutes = new LinkedHashSet<>();
      _areas = new HashMap<>();
   }

   @JsonPropertyDescription("The OSPF areas contained in this process")
   public Map<Long, OspfArea> getAreas() {
      return _areas;
   }

   @JsonPropertyDescription("The routing policy applied to routes in the main RIB to determine which ones are to be exported into OSPF and how")
   public String getExportPolicy() {
      return _exportPolicy;
   }

   @JsonPropertyDescription("Generated IPV4 routes for the purpose of export into OSPF. These routes are not imported into the main RIB.")
   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   @JsonPropertyDescription("The reference bandwidth by which an interface's bandwidth is divided to determine its OSPF cost")
   public Double getReferenceBandwidth() {
      return _referenceBandwidth;
   }

   @JsonPropertyDescription("The router-id of this OSPF process")
   public Ip getRouterId() {
      return _routerId;
   }

   public void setAreas(Map<Long, OspfArea> areas) {
      _areas = areas;
   }

   public void setExportPolicy(String exportPolicy) {
      _exportPolicy = exportPolicy;
   }

   public void setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
   }

   public void setReferenceBandwidth(Double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
   }

   public void setRouterId(Ip id) {
      _routerId = id;
   }

}
