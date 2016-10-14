package org.batfish.datamodel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborListsDiff extends ConfigDiffElement {
   
   private static final String DIFF = "diff";
   private Set<String> _diff;
   
   @JsonCreator
   public NeighborListsDiff() {
   }
   
   public NeighborListsDiff(Map<Prefix, BgpNeighbor> a, Map<Prefix, BgpNeighbor> b) {
      super(GetNeighborIps(a), GetNeighborIps(b));
      _diff = new HashSet<>();
      
   }

   private static Set<String> GetNeighborIps(Map<Prefix, BgpNeighbor> a) {
      Set<String> ips = new HashSet<>();
      for (BgpNeighbor bgpNeighbor: a.values()) {
         ips.add(bgpNeighbor.getAddress().toString());
      }
      return ips;
   }
   
   @JsonProperty(DIFF)
   public Set<String> getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> diff) {
      _diff = diff;
   }

}
