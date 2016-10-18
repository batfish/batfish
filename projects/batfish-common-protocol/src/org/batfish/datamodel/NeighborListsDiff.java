package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborListsDiff extends ConfigDiffElement {

   private static final String DIFF = "diff";
   private Set<String> _diff;

   @JsonCreator
   public NeighborListsDiff() {
   }

   public NeighborListsDiff(Map<Prefix, BgpNeighbor> a,
         Map<Prefix, BgpNeighbor> b) {
      super(new TreeSet<>(), new TreeSet<>());
      _diff = new TreeSet<>();

      for (Prefix aPrefix : a.keySet()) {
         if (b.containsKey(aPrefix)) {
            if (a.get(aPrefix).equals(b.get(aPrefix))) {
               super._identical.add(a.get(aPrefix).getDescription());
            }
            else {
               _diff.add(a.get(aPrefix).getDescription());
            }
         }
         else {
            super._inAOnly.add(a.get(aPrefix).getDescription());
         }
      }

      for (Prefix bPrefix : b.keySet()) {
         if (!a.containsKey(bPrefix)) {
            super._inBOnly.add(a.get(bPrefix).getDescription());
         }
      }
   }

   @JsonProperty(DIFF)
   public Set<String> getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> diff) {
      _diff = diff;
   }

}
