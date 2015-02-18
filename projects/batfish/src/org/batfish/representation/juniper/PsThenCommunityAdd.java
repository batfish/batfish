package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;

public final class PsThenCommunityAdd extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsThenCommunityAdd(String name) {
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
