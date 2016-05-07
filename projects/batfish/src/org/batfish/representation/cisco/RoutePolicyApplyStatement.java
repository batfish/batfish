package org.batfish.representation.cisco;

public class RoutePolicyApplyStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private String _applyName;

   public RoutePolicyApplyStatement(String name) {
      _applyName = name;
   }

   public String getName() {
      return _applyName;
   }

   @Override
   public RoutePolicyStatementType getType() {
      return RoutePolicyStatementType.APPLY;
   }

}
