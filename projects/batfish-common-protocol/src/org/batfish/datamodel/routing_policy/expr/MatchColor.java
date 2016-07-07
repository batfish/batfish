package org.batfish.datamodel.routing_policy.expr;

public class MatchColor extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _color;

   public MatchColor(int color) {
      _color = color;
   }

   public int getColor() {
      return _color;
   }

   public void setColor(int color) {
      _color = color;
   }

}
