package batfish.representation.juniper;

import batfish.representation.PolicyMapClause;
import batfish.representation.Prefix;

public class PsFromRouteFilterOrLonger extends PsFromRouteFilter {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public PsFromRouteFilterOrLonger(Prefix prefix) {
      super(prefix);
   }

   @Override
   public void applyTo(PolicyMapClause clause) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
