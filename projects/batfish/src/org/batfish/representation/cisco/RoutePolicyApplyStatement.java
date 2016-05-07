package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyApplyStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   private String _applyName;

   public RoutePolicyApplyStatement(String name) {
   		_applyName = name;
   }

   public RoutePolicyStatementType getType() { return RoutePolicyStatementType.APPLY; }

   public String getName() { return _applyName; }

}
