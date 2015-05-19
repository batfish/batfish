package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.representation.IsisLevel;
import org.batfish.representation.IsoAddress;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class IsisProcess implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisLevel _level;

   private IsoAddress _netAddress;

   private Map<RoutingProtocol, IsisRedistributionPolicy> _redistributionPolicies;

   private Set<Prefix> _summaryAddresses;

   public IsisProcess() {
      _redistributionPolicies = new TreeMap<RoutingProtocol, IsisRedistributionPolicy>();
      _summaryAddresses = new TreeSet<Prefix>();
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

   public Set<Prefix> getSummaryAddresses() {
      return _summaryAddresses;
   }

   public void setLevel(IsisLevel level) {
      _level = level;
   }

   public void setNetAddress(IsoAddress netAddress) {
      _netAddress = netAddress;
   }

}
