package org.batfish.representation.cisco;

import java.io.Serializable;

public abstract class RoutePolicyDeleteStatement extends RoutePolicyStatement {

   private static final long serialVersionUID = 1L;

   public RoutePolicyStatementType getType() { return RoutePolicyStatementType.DELETE; }

   public abstract RoutePolicyDeleteType getDeleteType();

}
