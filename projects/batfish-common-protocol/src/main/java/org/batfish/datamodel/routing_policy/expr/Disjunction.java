package org.batfish.datamodel.routing_policy.expr;

import com.google.common.collect.ImmutableList;
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
      childSources.addAll(disjunct.collectSources(parentSources, routingPolicies, w));
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
    if (_simplified != null) {
      return _simplified;
    }
    ImmutableList.Builder<BooleanExpr> simpleDisjunctsBuilder = ImmutableList.builder();
    boolean atLeastOneTrue = false;
    boolean atLeastOneComplex = false;
    for (BooleanExpr disjunct : _disjuncts) {
      BooleanExpr simpleDisjunct = disjunct.simplify();
      if (simpleDisjunct.equals(BooleanExprs.TRUE)) {
        atLeastOneTrue = true;
        if (!atLeastOneComplex) {
          _simplified = BooleanExprs.TRUE;
          return _simplified;
        } else if (!atLeastOneTrue) {
          simpleDisjunctsBuilder.add(simpleDisjunct);
        }
      } else if (!simpleDisjunct.equals(BooleanExprs.FALSE)) {
        atLeastOneComplex = true;
        simpleDisjunctsBuilder.add(simpleDisjunct);
      }
    }
    List<BooleanExpr> simpleDisjuncts = simpleDisjunctsBuilder.build();
    if (simpleDisjuncts.isEmpty()) {
      _simplified = BooleanExprs.FALSE;
    } else if (simpleDisjuncts.size() == 1) {
      _simplified = simpleDisjuncts.get(0);
    } else {
      Disjunction simple = new Disjunction();
      simple.setDisjuncts(simpleDisjuncts);
      simple.setComment(getComment());
      _simplified = simple;
      simple._simplified = _simplified;
    }
    return _simplified;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _disjuncts + ">";
  }
}
