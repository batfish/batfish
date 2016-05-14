package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.main.Warnings;

public final class PsThenAccept extends PsThen {

   public static final PsThenAccept INSTANCE = new PsThenAccept();

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private PsThenAccept() {
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings) {
      clause.setAction(PolicyMapAction.PERMIT);
   }

}
