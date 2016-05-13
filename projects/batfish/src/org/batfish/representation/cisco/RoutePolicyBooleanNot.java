package org.batfish.representation.cisco;

public class RoutePolicyBooleanNot extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   private RoutePolicyBoolean _operand;

   public RoutePolicyBooleanNot(RoutePolicyBoolean operand) {
      _operand = operand;
   }

   public RoutePolicyBoolean getOperand() {
      return _operand;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.NOT;
   }

}
