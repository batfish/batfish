package batfish.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class OspfProcess implements Serializable {

   private static final long serialVersionUID = 1L;

   private Map<Long, OspfArea> _areas;
   private Set<GeneratedRoute> _generatedRoutes;
   private Set<PolicyMap> _outboundPolicyMaps;
   private Double _referenceBandwidth;
   private String _routerId;

   public OspfProcess() {
      _generatedRoutes = new LinkedHashSet<GeneratedRoute>();
      _outboundPolicyMaps = new LinkedHashSet<PolicyMap>();
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

   public double getReferenceBandwidth() {
      return _referenceBandwidth;
   }

   public String getRouterId() {
      return _routerId;
   }

   public boolean sameParseTree(OspfProcess ospf, String prefix) {
      boolean res = true;
      boolean finalRes = res;

      if (_routerId != null) {
         res = res && (_routerId.equals(ospf._routerId));
      }
      else {
         res = res && (ospf._routerId == null);
      }
      if (res == false) {
         System.out.println("Ospf:RouterId " + prefix);
         finalRes = res;
      }

      if (_referenceBandwidth != null) {
         res = res && (_referenceBandwidth.equals(ospf._referenceBandwidth));
      }
      else {
         res = res && (ospf._referenceBandwidth == null);
      }

      if (res == false) {
         System.out.println("Ospf:RefBandwidth " + prefix);
         finalRes = res;
      }

      if (_areas.size() != ospf._areas.size()) {
         System.out.println("Ospf:Area:Size " + prefix);
         finalRes = false;
      }
      else {
         for (OspfArea lhs : _areas.values()) {
            OspfArea rhs = ospf._areas.get(lhs.getNumber());
            if (rhs == null) {
               System.out.println("Ospf:Area:NullRhs " + prefix);
               finalRes = false;
            }
            else {
               res = lhs.sameParseTree(rhs, "Ospf:Area " + prefix);
               if (res == false) {
                  finalRes = res;
               }
            }
         }
      }

      if (_generatedRoutes.size() != ospf._generatedRoutes.size()) {
         System.out.println("Ospf:GenRoute:Size " + prefix);
         finalRes = false;
      }
      for (GeneratedRoute lhs : _generatedRoutes) {
         boolean found = false;
         for (GeneratedRoute rhs : ospf._generatedRoutes) {
            if (lhs.equals(rhs)) {
               res = lhs.sameParseTree(rhs, "Ospf:GenRoute " + prefix);
               found = true;
               if (res == false) {
                  finalRes = res;
               }
               break;
            }
         }
         if (found == false) {
            System.out.println("Ospf:GenRoute:NotFound " + prefix);
            finalRes = false;
         }
      }

      if (_outboundPolicyMaps.size() != ospf._outboundPolicyMaps.size()) {
         System.out.println("Ospf:OutboundPoliMap:Size " + prefix);
         finalRes = false;
      }
      else {
         for (PolicyMap lhs : _outboundPolicyMaps) {
            boolean found = false;
            for (PolicyMap rhs : ospf._outboundPolicyMaps) {
               if (lhs.getMapName().equals(rhs.getMapName())) {
                  res = lhs
                        .sameParseTree(rhs, "Ospf:OutboundPoliMap " + prefix);
                  found = true;
                  if (res == false) {
                     finalRes = res;
                  }
                  break;
               }
            }
            if (found == false) {
               System.out.println("Ospf:OutboundPoliMap:NotFound " + prefix);
               finalRes = false;
            }
         }
      }

      return finalRes;
   }

   public void setReferenceBandwidth(double referenceBandwidth) {
      _referenceBandwidth = referenceBandwidth;
   }

   public void setRouterId(String id) {
      _routerId = id;
   }

}
