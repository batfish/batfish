package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.IsisLevel;
import org.batfish.representation.IsoAddress;
import org.batfish.representation.RoutingProtocol;

public class IsisProcess implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisLevel _level;

   private IsoAddress _netAddress;

   private Map<RoutingProtocol, IsisRedistributionPolicy> _redistributionPolicies;

   public IsisProcess() {
      _redistributionPolicies = new TreeMap<RoutingProtocol, IsisRedistributionPolicy>();
   }

   public IsisLevel getLevel() {
      return _level;
   }

   public IsoAddress getNetAddress() {
      return _netAddress;
   }

   public Map<RoutingProtocol, IsisRedistributionPolicy> getRedistributionPolicies() {
      return _redistributionPolicies;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }

   public void setNetAddress(IsoAddress netAddress) {
      _netAddress = netAddress;
   }

}
