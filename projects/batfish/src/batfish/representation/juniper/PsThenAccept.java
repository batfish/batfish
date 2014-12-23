package batfish.representation.juniper;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapAction;
import batfish.representation.PolicyMapClause;

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
