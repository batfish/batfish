package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NamedPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.common.Warnings;

public class RoutePolicyPrefixSetName extends RoutePolicyPrefixSet {

   private static final long serialVersionUID = 1L;

   private final int _expressionLine;

   private final String _name;

   public RoutePolicyPrefixSetName(String name, int expressionLine) {
      _name = name;
      _expressionLine = expressionLine;
   }

   public String getName() {
      return _name;
   }

   @Override
   public Prefix6SetExpr toPrefix6SetExpr(CiscoConfiguration cc,
         Configuration c, Warnings w) {
      if (cc.getPrefixLists().containsKey(_name)) {
         return null;
      }
      else if (!cc.getPrefix6Lists().containsKey(_name)) {
         cc.undefined(CiscoStructureType.PREFIX6_LIST, _name,
               CiscoStructureUsage.ROUTE_POLICY_PREFIX_SET, _expressionLine);
      }
      else {
         Prefix6List list = cc.getPrefix6Lists().get(_name);
         list.getReferers().put(this,
               "route policy named ipv6 prefix-set: '" + _name + "'");
      }
      return new NamedPrefix6Set(_name);
   }

   @Override
   public PrefixSetExpr toPrefixSetExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      if (cc.getPrefix6Lists().containsKey(_name)) {
         return null;
      }
      else if (!cc.getPrefixLists().containsKey(_name)) {
         cc.undefined(CiscoStructureType.PREFIX_LIST, _name,
               CiscoStructureUsage.ROUTE_POLICY_PREFIX_SET, _expressionLine);
      }
      else {
         PrefixList list = cc.getPrefixLists().get(_name);
         list.getReferers().put(this,
               "route policy named ipv4 prefix-set: '" + _name + "'");
      }
      return new NamedPrefixSet(_name);
   }

}
