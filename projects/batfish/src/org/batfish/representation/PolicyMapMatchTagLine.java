package org.batfish.representation;

import java.util.Set;

public class PolicyMapMatchTagLine extends PolicyMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<Integer> _tags;

   public PolicyMapMatchTagLine(Set<Integer> tags) {
      _tags = tags;
   }

   public Set<Integer> getTags() {
      return _tags;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.TAG;
   }

}
