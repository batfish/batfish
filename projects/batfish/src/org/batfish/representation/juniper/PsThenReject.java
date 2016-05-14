package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.main.Warnings;

public final class PsThenReject extends PsThen {

   public static final PsThenReject INSTANCE = new PsThenReject();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PsThenReject() {
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      clause.setAction(PolicyMapAction.DENY);
   }

}
