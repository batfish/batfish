package org.batfish.representation.cisco;

public class RoutePolicyBooleanAnd extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   private RoutePolicyBoolean _left;
   private RoutePolicyBoolean _right;

   public RoutePolicyBooleanAnd(RoutePolicyBoolean left,
         RoutePolicyBoolean right) {
      _left = left;
      _right = right;
   }

   public RoutePolicyBoolean getLeft() {
      return _left;
   }

   public RoutePolicyBoolean getRight() {
      return _right;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.AND;
   }

}
