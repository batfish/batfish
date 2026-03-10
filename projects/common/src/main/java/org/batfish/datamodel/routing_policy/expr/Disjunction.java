package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/**
 * Boolean expression that evaluates to true if any {@link BooleanExpr} in a given list evaluates to
 * true. Evaluates to false if the given list is empty.
 */
public final class Disjunction extends BooleanExpr {
  private static final String PROP_DISJUNCTS = "disjuncts";

  private List<BooleanExpr> _disjuncts;

  public Disjunction() {
    this(new ArrayList<>());
  }

  public Disjunction(BooleanExpr... disjuncts) {
    this(Arrays.asList(disjuncts));
  }

  public Disjunction(List<BooleanExpr> disjuncts) {
    _disjuncts = disjuncts;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitDisjunction(this, arg);
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
  public Result evaluate(Environment environment) {
    for (BooleanExpr disjunct : _disjuncts) {
      Result disjunctResult = disjunct.evaluate(environment);
      if (disjunctResult.getExit()) {
        return disjunctResult;
      } else if (disjunctResult.getBooleanValue()) {
        return disjunctResult.toBuilder().setReturn(false).build();
      }
    }
    return new Result(false);
  }

  @JsonProperty(PROP_DISJUNCTS)
  public List<BooleanExpr> getDisjuncts() {
    return _disjuncts;
  }

  @JsonProperty(PROP_DISJUNCTS)
  public void setDisjuncts(List<BooleanExpr> disjuncts) {
    _disjuncts = disjuncts;
  }

  @Override
  public BooleanExpr simplify() {
    if (_simplified != null) {
      return _simplified;
    }
    ImmutableList.Builder<BooleanExpr> simpleDisjunctsBuilder = ImmutableList.builder();
    for (BooleanExpr disjunct : _disjuncts) {
      BooleanExpr simpleDisjunct = disjunct.simplify();
      if (simpleDisjunct.equals(BooleanExprs.FALSE)) {
        // Skip false.
        continue;
      }
      simpleDisjunctsBuilder.add(simpleDisjunct);
      if (simpleDisjunct.equals(BooleanExprs.TRUE)) {
        // Short-circuit the disjunction after the last true.
        break;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Disjunction)) {
      return false;
    }
    Disjunction other = (Disjunction) obj;
    return Objects.equals(_disjuncts, other._disjuncts);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_disjuncts);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _disjuncts + ">";
  }
}
