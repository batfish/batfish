package org.batfish.datamodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


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

   public Map<Long, OspfArea> getAreas() {
      return _areas;
   }

   public String getExportPolicy() {
      return _exportPolicy;
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public Double getReferenceBandwidth() {
      return _referenceBandwidth;
   }

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
