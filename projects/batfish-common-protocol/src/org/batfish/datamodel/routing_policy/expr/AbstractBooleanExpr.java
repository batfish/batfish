package org.batfish.datamodel.routing_policy.expr;

public abstract class AbstractBooleanExpr implements BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _comment;

   public String getComment() {
      return _comment;
   }

   public void setComment(String comment) {
      _comment = comment;
   }

   @Override
   public BooleanExpr simplify() {
      return this;
   }

   @Override
   public String toString() {
      if (_comment != null) {
         return getClass().getSimpleName() + "<" + _comment + ">";
      }
      else {
         return super.toString();
      }
   }

}
