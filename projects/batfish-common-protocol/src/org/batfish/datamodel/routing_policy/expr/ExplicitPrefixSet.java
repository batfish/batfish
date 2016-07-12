package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.PrefixSpace;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitPrefixSet implements PrefixSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixSpace _prefixSpace;

   @JsonCreator
   public ExplicitPrefixSet() {
   }

   public ExplicitPrefixSet(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
   }

   public PrefixSpace getPrefixSpace() {
      return _prefixSpace;
   }

   public void setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
   }

}
