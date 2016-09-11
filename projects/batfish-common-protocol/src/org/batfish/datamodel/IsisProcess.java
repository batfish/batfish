package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class IsisProcess implements Serializable {

   public static final int DEFAULT_ISIS_INTERFACE_COST = 10;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Set<GeneratedRoute> _generatedRoutes;

   private IsisLevel _level;

   private IsoAddress _netAddress;

   private List<PolicyMap> _outboundPolicyMaps;

   private SortedMap<String, IsisLevel> _policyExportLevels;

   public IsisProcess() {
      _generatedRoutes = new LinkedHashSet<>();
      _policyExportLevels = new TreeMap<>();
      _outboundPolicyMaps = new ArrayList<>();
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

   @JsonIdentityReference(alwaysAsId = true)
   public List<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   public SortedMap<String, IsisLevel> getPolicyExportLevels() {
      return _policyExportLevels;
   }

   public void setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }

   public void setNetAddress(IsoAddress netAddress) {
      _netAddress = netAddress;
   }

   public void setOutboundPolicyMaps(List<PolicyMap> outboundPolicyMaps) {
      _outboundPolicyMaps = outboundPolicyMaps;
   }

   public void setPolicyExportLevels(
         SortedMap<String, IsisLevel> policyExportLevels) {
      _policyExportLevels = policyExportLevels;
   }

}
