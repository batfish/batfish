package org.batfish.datamodel.routing_policy.expr;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class Disjunction extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private List<BooleanExpr> _disjuncts;

  public Disjunction() {
    _disjuncts = new ArrayList<>();
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
    for (BooleanExpr disjunct : _disjuncts) {
      childSources.addAll(disjunct.collectSources(parentSources, routingPolicies, w).iterator());
    }
    return childSources.build();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Disjunction other = (Disjunction) obj;
    if (_disjuncts == null) {
      if (other._disjuncts != null) {
        return false;
      }
    } else if (!_disjuncts.equals(other._disjuncts)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    for (BooleanExpr disjunct : _disjuncts) {
      Result disjunctResult = disjunct.evaluate(environment);
      if (disjunctResult.getExit()) {
        return disjunctResult;
      } else if (disjunctResult.getBooleanValue()) {
        disjunctResult.setReturn(false);
        return disjunctResult;
      }
    }
    Result result = new Result();
    result.setBooleanValue(false);
    return result;
  }

  public List<BooleanExpr> getDisjuncts() {
    return _disjuncts;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_disjuncts == null) ? 0 : _disjuncts.hashCode());
    return result;
  }

  public void setDisjuncts(List<BooleanExpr> disjuncts) {
    _disjuncts = disjuncts;
  }

  @Override
  public BooleanExpr simplify() {
    List<BooleanExpr> simpleDisjuncts = new ArrayList<>();
    boolean atLeastOneTrue = false;
    boolean atLeastOneComplex = false;
    for (BooleanExpr disjunct : _disjuncts) {
      BooleanExpr simpleDisjunct = disjunct.simplify();
      if (simpleDisjunct.equals(BooleanExprs.True.toStaticBooleanExpr())) {
        atLeastOneTrue = true;
        if (!atLeastOneComplex) {
          return BooleanExprs.True.toStaticBooleanExpr();
        } else if (!atLeastOneTrue) {
          simpleDisjuncts.add(simpleDisjunct);
        }
      } else if (!simpleDisjunct.equals(BooleanExprs.False.toStaticBooleanExpr())) {
        atLeastOneComplex = true;
        simpleDisjuncts.add(simpleDisjunct);
      }
    }

    if (simpleDisjuncts.isEmpty()) {
      return BooleanExprs.False.toStaticBooleanExpr();
    } else if (simpleDisjuncts.size() == 1) {
      return simpleDisjuncts.get(0);
    } else {
      Disjunction simple = new Disjunction();
      simple.setDisjuncts(simpleDisjuncts);
      simple.setComment(getComment());
      return simple;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _disjuncts + ">";
  }
}
