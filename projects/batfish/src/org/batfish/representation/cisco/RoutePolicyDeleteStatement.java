package org.batfish.representation.cisco;

public abstract class RoutePolicyDeleteStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   public abstract RoutePolicyDeleteType getDeleteType();

   @Override
   public RoutePolicyStatementType getType() {
      return RoutePolicyStatementType.DELETE;
   }

}
