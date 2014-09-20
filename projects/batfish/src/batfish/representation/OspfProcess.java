package batfish.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class OspfProcess implements Serializable {

   private static final long serialVersionUID = 1L;

   private Map<Long, OspfArea> _areas;
   private Set<GeneratedRoute> _generatedRoutes;
   private Set<PolicyMap> _outboundPolicyMaps;
   private Map<PolicyMap, OspfMetricType> _policyMetricTypes;
   private Double _referenceBandwidth;
   private String _routerId;

   public OspfProcess() {
      _generatedRoutes = new TreeSet<GeneratedRoute>();
      _outboundPolicyMaps = new TreeSet<PolicyMap>();
      _policyMetricTypes = new TreeMap<PolicyMap, OspfMetricType>();
      _referenceBandwidth = null;
      _routerId = null;
      _areas = new HashMap<Long, OspfArea>();
   }

   public Map<Long, OspfArea> getAreas() {
      return _areas;
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public Set<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   public Map<PolicyMap, OspfMetricType> getPolicyMetricTypes() {
      return _policyMetricTypes;
   }

   public double getReferenceBandwidth() {
      return _referenceBandwidth;
   }

   public String getRouterId() {
      return _routerId;
   }

   public void setReferenceBandwidth(double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
   }

   public void setRouterId(String id) {
      _routerId = id;
   }

}
