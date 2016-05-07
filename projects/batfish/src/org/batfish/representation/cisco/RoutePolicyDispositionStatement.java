package org.batfish.representation.cisco;

public class RoutePolicyDispositionStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyDispositionType _dispositionType;

   public RoutePolicyDispositionStatement(RoutePolicyDispositionType dType) {
      _dispositionType = dType;
   }

   public RoutePolicyDispositionType getDispositionType() {
      return _dispositionType;
   }

   @Override
   public RoutePolicyStatementType getType() {
      return RoutePolicyStatementType.DISPOSITION;
   }

}
