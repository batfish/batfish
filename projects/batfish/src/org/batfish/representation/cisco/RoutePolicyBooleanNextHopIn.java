package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanNextHopIn extends RoutePolicyBoolean {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private RoutePolicyPrefixSet _prefixSet;

   public RoutePolicyBooleanNextHopIn(RoutePolicyPrefixSet prefixSet) {
      _prefixSet = prefixSet;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new MatchPrefixSet(
            new IpPrefix(new NextHopIp(),
                  new LiteralInt(Prefix.MAX_PREFIX_LENGTH)),
            _prefixSet.toPrefixSetExpr(cc, c, w));
   }

}
