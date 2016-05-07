package org.batfish.representation.cisco;

public abstract class RoutePolicySetStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   public abstract RoutePolicySetType getSetType();

   @Override
   public RoutePolicyStatementType getType() {
      return RoutePolicyStatementType.SET;
   }

}
