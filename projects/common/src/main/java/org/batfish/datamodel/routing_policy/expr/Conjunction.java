package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/**
 * Boolean expression that evaluates to true if every {@link BooleanExpr} in a given list evaluates
 * to true. Evaluates to true if the given list is empty.
 */
public final class Conjunction extends BooleanExpr {

  private static final String PROP_CONJUNCTS = "conjuncts";

  private List<BooleanExpr> _conjuncts;

  public Conjunction() {
    this(new ArrayList<>());
  }

  @JsonCreator
  public Conjunction(@JsonProperty("PROP_CONJUNCTS") List<BooleanExpr> conjuncts) {
    _conjuncts = firstNonNull(conjuncts, Collections.emptyList());
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitConjunction(this, arg);
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
  public Result evaluate(Environment environment) {
    for (BooleanExpr conjunct : _conjuncts) {
      Result conjunctResult = conjunct.evaluate(environment);
      if (conjunctResult.getExit()) {
        return conjunctResult;
      } else if (!conjunctResult.getBooleanValue()) {
        return conjunctResult.toBuilder().setReturn(false).build();
      }
    }
    return new Result(true);
  }

  @JsonProperty(PROP_CONJUNCTS)
  public List<BooleanExpr> getConjuncts() {
    return _conjuncts;
  }

  @JsonProperty(PROP_CONJUNCTS)
  public void setConjuncts(List<BooleanExpr> conjuncts) {
    _conjuncts = conjuncts;
  }

  @Override
  public BooleanExpr simplify() {
    if (_simplified != null) {
      return _simplified;
    }
    ImmutableList.Builder<BooleanExpr> simpleConjunctsBuilder = ImmutableList.builder();
    for (BooleanExpr conjunct : _conjuncts) {
      BooleanExpr simpleConjunct = conjunct.simplify();
      if (simpleConjunct.equals(BooleanExprs.TRUE)) {
        // Skip true.
        continue;
      }
      simpleConjunctsBuilder.add(simpleConjunct);
      if (simpleConjunct.equals(BooleanExprs.FALSE)) {
        // Short-circuit the conjunction after the first false.
        break;
      }
    }
    List<BooleanExpr> simpleConjuncts = simpleConjunctsBuilder.build();
    if (simpleConjuncts.isEmpty()) {
      _simplified = BooleanExprs.TRUE;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Conjunction)) {
      return false;
    }
    Conjunction other = (Conjunction) obj;
    return Objects.equals(_conjuncts, other._conjuncts);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_conjuncts);
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_CONJUNCTS, _conjuncts).toString();
  }
}
