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

   public int getList() {
      return _color;
   }

   public void setList(int color) {
      _color = color;
   }

}
