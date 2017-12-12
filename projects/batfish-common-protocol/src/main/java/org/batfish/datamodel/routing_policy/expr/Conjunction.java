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

public class Conjunction extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private List<BooleanExpr> _conjuncts;

  public Conjunction() {
    _conjuncts = new ArrayList<>();
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
    for (BooleanExpr conjunct : _conjuncts) {
      childSources.addAll(conjunct.collectSources(parentSources, routingPolicies, w));
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
    Conjunction other = (Conjunction) obj;
    if (_conjuncts == null) {
      if (other._conjuncts != null) {
        return false;
      }
    } else if (!_conjuncts.equals(other._conjuncts)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    for (BooleanExpr conjunct : _conjuncts) {
      Result conjunctResult = conjunct.evaluate(environment);
      if (conjunctResult.getExit()) {
        return conjunctResult;
      } else if (!conjunctResult.getBooleanValue()) {
        conjunctResult.setReturn(false);
        return conjunctResult;
      }
    }
    Result result = new Result();
    result.setBooleanValue(true);
    return result;
  }

  public List<BooleanExpr> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_conjuncts == null) ? 0 : _conjuncts.hashCode());
    return result;
  }

  public void setConjuncts(List<BooleanExpr> conjuncts) {
    _conjuncts = conjuncts;
  }

  @Override
  public BooleanExpr simplify() {
    if (_simplified != null) {
      return _simplified;
    }
    ImmutableList.Builder<BooleanExpr> simpleConjunctsBuilder = ImmutableList.builder();
    boolean atLeastOneFalse = false;
    boolean atLeastOneComplex = false;
    for (BooleanExpr conjunct : _conjuncts) {
      BooleanExpr simpleConjunct = conjunct.simplify();
      if (simpleConjunct.equals(BooleanExprs.False.toStaticBooleanExpr())) {
        atLeastOneFalse = true;
        if (!atLeastOneComplex) {
          _simplified = BooleanExprs.False.toStaticBooleanExpr();
          return _simplified;
        } else if (!atLeastOneFalse) {
          simpleConjunctsBuilder.add(simpleConjunct);
        }
      } else if (!simpleConjunct.equals(BooleanExprs.True.toStaticBooleanExpr())) {
        atLeastOneComplex = true;
        simpleConjunctsBuilder.add(simpleConjunct);
      }
    }
    List<BooleanExpr> simpleConjuncts = simpleConjunctsBuilder.build();
    if (simpleConjuncts.isEmpty()) {
      _simplified = BooleanExprs.True.toStaticBooleanExpr();
    } else if (simpleConjuncts.size() == 1) {
      _simplified = simpleConjuncts.get(0);
    } else {
      Conjunction simple = new Conjunction();
      simple.setConjuncts(simpleConjuncts);
      simple.setComment(getComment());
      _simplified = simple;
      simple._simplified = _simplified;
    }
    return _simplified;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _conjuncts + ">";
  }
}
