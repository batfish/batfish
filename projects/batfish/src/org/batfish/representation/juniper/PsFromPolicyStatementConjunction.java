package org.batfish.representation.juniper;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchPolicyConjunctionLine;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.MatchRoutingPolicy;
import org.batfish.main.Warnings;

public final class PsFromPolicyStatementConjunction extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<String> _conjuncts;

   public PsFromPolicyStatementConjunction(Set<String> conjuncts) {
      _conjuncts = conjuncts;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      Set<PolicyMap> policies = new LinkedHashSet<PolicyMap>();
      for (String conjunctName : _conjuncts) {
         PolicyMap conjunct = c.getPolicyMaps().get(conjunctName);
         if (conjunct != null) {
            PolicyStatement conjunctPs = jc.getPolicyStatements().get(
                  conjunctName);
            if (conjunctPs.getIpv6()) {
               ps.setIpv6(true);
            }
            policies.add(conjunct);
         }
         else {
            warnings.redFlag("Reference to undefined policy conjunct: \""
                  + conjunctName + "\"");
         }
      }
      PolicyMapMatchPolicyConjunctionLine match = new PolicyMapMatchPolicyConjunctionLine(
            policies);
      clause.getMatchLines().add(match);
   }

   public Set<String> getConjuncts() {
      return _conjuncts;
   }

   @Override
   public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c,
         Warnings warnings) {
      Conjunction conj = new Conjunction();
      conj.getConjuncts().addAll(
            _conjuncts.stream()
                  .map(conjunct -> new MatchRoutingPolicy(conjunct))
                  .collect(Collectors.toList()));
      return conj;
   }

}
