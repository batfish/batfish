package batfish.representation.juniper;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapClause;

public class PsFromAsPath extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _asPathName;

   public PsFromAsPath(String asPathName) {
      _asPathName = asPathName;
   }

   @Override
   public void applyTo(PolicyMapClause clause, Configuration c) {
      // throw new
      // UnsupportedOperationException("no implementation for generated method");
      // TODO Auto-generated method stub
   }

   public String getAsPathName() {
      return _asPathName;
   }

}
