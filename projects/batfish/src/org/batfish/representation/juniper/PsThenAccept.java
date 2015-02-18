package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;

public final class PsThenAccept extends PsThen {

   public static final PsThenAccept INSTANCE = new PsThenAccept();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PsThenAccept() {
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      clause.setAction(PolicyMapAction.PERMIT);
   }

}
