package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchCommunityListLine;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.main.Warnings;

public final class PsFromCommunity extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsFromCommunity(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      org.batfish.datamodel.CommunityList list = c.getCommunityLists().get(
            _name);
      if (list == null) {
         warnings.redFlag("missing community list: \"" + _name + "\"");
      }
      else {
         PolicyMapMatchCommunityListLine line = new PolicyMapMatchCommunityListLine(
               Collections.singleton(list));
         clause.getMatchLines().add(line);
      }
   }

   public String getName() {
      return _name;
   }

   @Override
   public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c,
         Warnings warnings) {
      return new MatchCommunitySet(new NamedCommunitySet(_name));
   }

}
