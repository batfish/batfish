package org.batfish.representation.cisco;

import java.io.Serializable;

public abstract class RoutePolicyPrefixSetInline extends RoutePolicyPrefixSet {

   private static final long serialVersionUID = 1L;

   private Integer _lowerBound;
   private Integer _upperBound;

   RoutePolicyPrefixSetInline(Integer lowerBound, Integer upperBound) {
   	_lowerBound = lowerBound;
   	_upperBound = upperBound;
   }

   public abstract RoutePolicyPrefixType getPrefixType();

   public Integer getLowerBound() { return _lowerBound; }
   public Integer getUpperBound() { return _upperBound; }

}
