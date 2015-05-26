package org.batfish.representation.juniper;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapClauseMatchInterfaceLine;

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
      PolicyMapClauseMatchInterfaceLine line = new PolicyMapClauseMatchInterfaceLine(
            _name);
      clause.getMatchLines().add(line);
   }

   public String getName() {
      return _name;
   }

}
