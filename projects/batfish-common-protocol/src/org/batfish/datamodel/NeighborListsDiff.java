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
         BgpNeighbor aNeighbor = a.get(aPrefix);
         String aDescription = aNeighbor.getDescription();
         if (aDescription == null) {
            aDescription = aPrefix.toString();
         }
         if (b.containsKey(aPrefix)) {
            BgpNeighbor bNeighbor = b.get(aPrefix);
            if (aNeighbor.equals(bNeighbor)) {
               super._identical.add(aDescription);
            }
            else {
               _diff.add(aDescription);
            }
         }
         else {
            super._inAOnly.add(aDescription);
         }
      }

      for (Prefix bPrefix : b.keySet()) {
         BgpNeighbor bNeighbor = b.get(bPrefix);
         if (!a.containsKey(bPrefix)) {
            String bDescription = bNeighbor.getDescription();
            if (bDescription == null) {
               bDescription = bPrefix.toString();
            }
            super._inBOnly.add(bDescription);
         }
      }

      super.summarizeIdentical();
   }

   @JsonProperty(DIFF)
   public Set<String> getDiff() {
      return _diff;
   }

   public void setDiff(Set<String> diff) {
      _diff = diff;
   }

}
