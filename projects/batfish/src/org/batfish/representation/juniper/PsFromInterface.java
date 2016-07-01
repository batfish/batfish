package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapClauseMatchInterfaceLine;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchSourceInterface;
import org.batfish.main.Warnings;

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
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      PolicyMapClauseMatchInterfaceLine line = new PolicyMapClauseMatchInterfaceLine(
            _name);
      clause.getMatchLines().add(line);
   }

   public String getName() {
      return _name;
   }

   @Override
   public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c,
         Warnings warnings) {
      return new MatchSourceInterface(_name);
   }

}
