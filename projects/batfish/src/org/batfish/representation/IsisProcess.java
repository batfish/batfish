package org.batfish.representation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class IsisProcess implements Serializable {

   public static final int DEFAULT_ISIS_INTERFACE_COST = 10;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Set<GeneratedRoute> _generatedRoutes;

   private IsisLevel _level;

   private IsoAddress _netAddress;

   private Set<PolicyMap> _outboundPolicyMaps;

   private Map<PolicyMap, IsisLevel> _policyExportLevels;

   public IsisProcess() {
      _generatedRoutes = new LinkedHashSet<GeneratedRoute>();
      _policyExportLevels = new LinkedHashMap<PolicyMap, IsisLevel>();
      _outboundPolicyMaps = new LinkedHashSet<PolicyMap>();
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public IsisLevel getLevel() {
      return _level;
   }

   public IsoAddress getNetAddress() {
      return _netAddress;
   }

   public Set<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   public Map<PolicyMap, IsisLevel> getPolicyExportLevels() {
      return _policyExportLevels;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }

   public void setNetAddress(IsoAddress netAddress) {
      _netAddress = netAddress;
   }

}
