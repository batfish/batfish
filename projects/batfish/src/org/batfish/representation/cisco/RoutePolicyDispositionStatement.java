package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyDispositionStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyDispositionType _dispositionType;

   public RoutePolicyDispositionStatement(RoutePolicyDispositionType dType) {
   		_dispositionType = dType;
   }

   public RoutePolicyStatementType getType() { return RoutePolicyStatementType.DISPOSITION; }

   public RoutePolicyDispositionType getDispositionType() { return _dispositionType; }

}
