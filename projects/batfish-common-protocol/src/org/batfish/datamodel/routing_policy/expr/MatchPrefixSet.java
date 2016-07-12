package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchPrefixSet extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PrefixSetExpr _expr;

   @JsonCreator
   public MatchPrefixSet() {
   }

   public MatchPrefixSet(PrefixSetExpr expr) {
      _expr = expr;
   }

   public PrefixSetExpr getExpr() {
      return _expr;
   }

   public void setExpr(PrefixSetExpr expr) {
      _expr = expr;
   }

}
