package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;

public final class PsThenReject extends PsThen {

   public static final PsThenReject INSTANCE = new PsThenReject();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PsThenReject() {
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      clause.setAction(PolicyMapAction.DENY);
   }

}
