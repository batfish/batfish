package org.batfish.datamodel;

import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NeighborsDiff extends ConfigDiffElement {

   @JsonCreator
   private NeighborsDiff() {
   }

   public NeighborsDiff(Map<Prefix, BgpNeighbor> before,
         Map<Prefix, BgpNeighbor> after) {
      super(new TreeSet<>(), new TreeSet<>());

      for (Prefix beforePrefix : before.keySet()) {
         BgpNeighbor beforeNeighbor = before.get(beforePrefix);
         String beforeDescription = beforeNeighbor.getDescription();
         if (beforeDescription == null) {
            beforeDescription = beforePrefix.toString();
         }
         if (after.containsKey(beforePrefix)) {
            BgpNeighbor bNeighbor = after.get(beforePrefix);
            if (beforeNeighbor.equals(bNeighbor)) {
               super._identical.add(beforeDescription);
            }
            else {
               _diff.add(beforeDescription);
            }
         }
         else {
            super._inBeforeOnly.add(beforeDescription);
         }
      }

      for (Prefix afterPrefix : after.keySet()) {
         BgpNeighbor afterNeighbor = after.get(afterPrefix);
         if (!before.containsKey(afterPrefix)) {
            String afterDescription = afterNeighbor.getDescription();
            if (afterDescription == null) {
               afterDescription = afterPrefix.toString();
            }
            super._inAfterOnly.add(afterDescription);
         }
      }

      super.summarizeIdentical();
   }

}
