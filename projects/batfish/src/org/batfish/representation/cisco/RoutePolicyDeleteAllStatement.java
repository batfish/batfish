package org.batfish.representation.cisco;

public class RoutePolicyDeleteAllStatement extends RoutePolicyDeleteStatement {

   private static final long serialVersionUID = 1L;

   @Override
   public RoutePolicyDeleteType getDeleteType() {
      return RoutePolicyDeleteType.ALL;
   }

}
