package org.batfish.datamodel.routing_policy.expr;

public class MatchTag extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _tag;

   public MatchTag(int tag) {
      _tag = tag;
   }

   public int getTag() {
      return _tag;
   }

   public void setTag(int tag) {
      _tag = tag;
   }

}
