package org.batfish.representation.juniper;

import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;

/** Represents a group of policy-statements that should be evaluated as a conjunction */
public final class PsFromPolicyStatementConjunction extends PsFrom {

  private final Set<String> _conjuncts;

  public PsFromPolicyStatementConjunction(Set<String> conjuncts) {
    _conjuncts = conjuncts;
  }

  public Set<String> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    Conjunction conj = new Conjunction();
    for (String conjunct : _conjuncts) {
      PolicyStatement conjunctPs = jc.getMasterLogicalSystem().getPolicyStatements().get(conjunct);
      if (conjunctPs != null) {
        conj.getConjuncts().add(new CallExpr(conjunct));
      } else {
        warnings.redFlag("Reference to undefined policy conjunct: \"" + conjunct + "\"");
      }
    }
    return conj;
  }
}
