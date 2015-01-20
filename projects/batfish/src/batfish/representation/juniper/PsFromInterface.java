package batfish.representation.juniper;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapClause;

public final class PsFromInterface extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsFromInterface(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public String getName() {
      return _name;
   }

}
