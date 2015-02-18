package org.batfish.representation;

import java.util.List;

public class PolicyMapSetAsPathPrependLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Integer> _asList;

   public PolicyMapSetAsPathPrependLine(List<Integer> asList) {
      _asList = asList;
   }

   public List<Integer> getAsList() {
      return _asList;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.AS_PATH_PREPEND;
   }

}
