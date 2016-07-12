package org.batfish.representation.juniper;

import java.util.Collections;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.main.Warnings;

public final class PsFromPrefixList extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   public PsFromPrefixList(String name) {
      _name = name;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      RouteFilterList list = c.getRouteFilterLists().get(_name);
      if (list == null) {
         warnings.redFlag("Reference to undefined route filter list: \""
               + _name + "\"");
      }
      else {
         PrefixList prefixList = jc.getPrefixLists().get(_name);
         if (prefixList.getIpv6()) {
            ps.setIpv6(true);
         }
         prefixList.getReferers().put(this,
               "policy-statement match prefix-list");
         PolicyMapMatchRouteFilterListLine line = new PolicyMapMatchRouteFilterListLine(
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
      return new MatchPrefixSet(new NamedPrefixSet(_name));
   }

}
