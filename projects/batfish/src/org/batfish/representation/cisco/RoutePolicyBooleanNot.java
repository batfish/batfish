package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyBooleanNot extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   private RoutePolicyBoolean _operand;

   public RoutePolicyBooleanNot(RoutePolicyBoolean operand) {
   		_operand = operand;
   }

   public RoutePolicyBooleanType getType() { return RoutePolicyBooleanType.NOT; }

   public RoutePolicyBoolean getOperand() { return _operand; }

}
