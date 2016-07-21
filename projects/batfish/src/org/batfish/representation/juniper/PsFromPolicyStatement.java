package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchPolicyLine;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.main.Warnings;

public final class PsFromPolicyStatement extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _policyStatement;

   public PsFromPolicyStatement(String policyStatement) {
      _policyStatement = policyStatement;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      PolicyMap policy = c.getPolicyMaps().get(_policyStatement);
      if (policy != null) {
         PolicyStatement subPs = jc.getPolicyStatements().get(_policyStatement);
         if (subPs.getIpv6()) {
            ps.setIpv6(true);
         }
         PolicyMapMatchPolicyLine match = new PolicyMapMatchPolicyLine(policy);
         clause.getMatchLines().add(match);
      }
      else {
         warnings.redFlag("Reference to undefined policy conjunct: \""
               + _policyStatement + "\"");
      }
   }

   public String getPolicyStatement() {
      return _policyStatement;
   }

   @Override
   public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c,
         Warnings warnings) {
      return new MatchPrefixSet(new NamedPrefixSet(_policyStatement));
   }

}
