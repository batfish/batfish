package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyDeleteAllStatement extends RoutePolicyDeleteStatement {

   private static final long serialVersionUID = 1L;

   public RoutePolicyDeleteType getDeleteType() { return RoutePolicyDeleteType.ALL; }

}
