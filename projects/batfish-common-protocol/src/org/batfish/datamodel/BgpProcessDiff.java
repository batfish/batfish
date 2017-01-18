package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BgpProcessDiff {

   private static final String IN_AFTER_ONLY_VAR = "inAfterOnly";

   private static final String IN_BEFORE_ONLY_VAR = "inBeforeOnly";

   private Boolean _inAfterOnly;

   private Boolean _inBeforeOnly;

   private NeighborsDiff _neighborsDiff;

   @JsonCreator
   public BgpProcessDiff() {
   }

   public BgpProcessDiff(BgpProcess before, BgpProcess after) {
      if (before != null && after == null) {
         _inBeforeOnly = true;
      }
      else if (before == null && after != null) {
         _inAfterOnly = true;
      }
      else if (before != null && after != null) {
         _neighborsDiff = new NeighborsDiff(before.getNeighbors(),
               after.getNeighbors());
         if (_neighborsDiff.isEmpty()) {
            _neighborsDiff = null;
         }
      }
   }

   @JsonProperty(IN_AFTER_ONLY_VAR)
   public Boolean getInAfterOnly() {
      return _inAfterOnly;
   }

   @JsonProperty(IN_BEFORE_ONLY_VAR)
   public Boolean getInBeforeOnly() {
      return _inBeforeOnly;
   }

   @JsonIgnore
   public boolean isEmpty() {
      return _neighborsDiff == null && _inBeforeOnly == null
            && _inAfterOnly == null;
   }

   @JsonProperty(IN_AFTER_ONLY_VAR)
   public void setInAfterOnly(Boolean inAfterOnly) {
      _inAfterOnly = inAfterOnly;
   }

   @JsonProperty(IN_BEFORE_ONLY_VAR)
   public void setInBeforeOnly(Boolean inBeforeOnly) {
      _inBeforeOnly = inBeforeOnly;
   }

}
