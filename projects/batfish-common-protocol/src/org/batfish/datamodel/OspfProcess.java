package org.batfish.datamodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class OspfProcess implements Serializable {

   private static final long serialVersionUID = 1L;

   private Map<Long, OspfArea> _areas;

   private Set<GeneratedRoute> _generatedRoutes;

   private Set<PolicyMap> _outboundPolicyMaps;

   private Map<String, OspfMetricType> _policyMetricTypes;

   private Double _referenceBandwidth;

   private Ip _routerId;

   public OspfProcess() {
      _generatedRoutes = new LinkedHashSet<GeneratedRoute>();
      _outboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _policyMetricTypes = new LinkedHashMap<String, OspfMetricType>();
      _referenceBandwidth = null;
      _routerId = null;
      _areas = new HashMap<Long, OspfArea>();
   }

   public Map<Long, OspfArea> getAreas() {
      return _areas;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   public Map<String, OspfMetricType> getPolicyMetricTypes() {
      return _policyMetricTypes;
   }

   public double getReferenceBandwidth() {
      return _referenceBandwidth;
   }

   public Ip getRouterId() {
      return _routerId;
   }

   public void setReferenceBandwidth(double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
   }

   public void setRouterId(Ip id) {
      _routerId = id;
   }

}
