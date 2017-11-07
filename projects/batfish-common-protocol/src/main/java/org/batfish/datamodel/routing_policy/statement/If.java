package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

public class If extends Statement {

  /** */
  private static final long serialVersionUID = 1L;

  private List<Statement> _falseStatements;

  private BooleanExpr _guard;

  private List<Statement> _trueStatements;

  @JsonCreator
  public If() {
    _falseStatements = new ArrayList<>();
    _trueStatements = new ArrayList<>();
  }

  @Override
  public void collectSources(
      Set<String> sources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    for (Statement statement : _falseStatements) {
      statement.collectSources(sources, routingPolicies, w);
    }
    for (Statement statement : _trueStatements) {
      statement.collectSources(sources, routingPolicies, w);
    }
    if (_guard != null) {
      _guard.collectSources(sources, routingPolicies, w);
    }
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
    If other = (If) obj;
    if (_falseStatements == null) {
      if (other._falseStatements != null) {
        return false;
      }
    } else if (!_falseStatements.equals(other._falseStatements)) {
      return false;
    }
    if (_guard == null) {
      if (other._guard != null) {
        return false;
      }
    } else if (!_guard.equals(other._guard)) {
      return false;
    }
    if (_trueStatements == null) {
      if (other._trueStatements != null) {
        return false;
      }
    } else if (!_trueStatements.equals(other._trueStatements)) {
      return false;
    }
    return true;
  }

  @Override
  public Result execute(Environment environment) {
    Result exprResult = _guard.evaluate(environment);
    if (exprResult.getExit()) {
      return exprResult;
    }
    boolean guardVal = exprResult.getBooleanValue();
    List<Statement> toExecute = guardVal ? _trueStatements : _falseStatements;
    for (Statement statement : toExecute) {
      Result result = statement.execute(environment);
      if (result.getExit() || result.getReturn()) {
        return result;
      }
    }
    Result fallThroughResult = new Result();
    fallThroughResult.setFallThrough(true);
    return fallThroughResult;
  }

  public List<Statement> getFalseStatements() {
    return _falseStatements;
  }

  public BooleanExpr getGuard() {
    return _guard;
  }

  public List<Statement> getTrueStatements() {
    return _trueStatements;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_falseStatements == null) ? 0 : _falseStatements.hashCode());
    result = prime * result + ((_guard == null) ? 0 : _guard.hashCode());
    result = prime * result + ((_trueStatements == null) ? 0 : _trueStatements.hashCode());
    return result;
  }

  public void setFalseStatements(List<Statement> falseStatements) {
    _falseStatements = falseStatements;
  }

  public void setGuard(BooleanExpr guard) {
    _guard = guard;
  }

  public void setTrueStatements(List<Statement> trueStatements) {
    _trueStatements = trueStatements;
  }

  @Override
  public List<Statement> simplify() {
    List<Statement> simpleTrueStatements = new ArrayList<>();
    List<Statement> simpleFalseStatements = new ArrayList<>();
    BooleanExpr simpleGuard = _guard.simplify();
    for (Statement trueStatement : _trueStatements) {
      simpleTrueStatements.addAll(trueStatement.simplify());
    }
    for (Statement falseStatement : _falseStatements) {
      simpleFalseStatements.addAll(falseStatement.simplify());
    }
    if (simpleGuard.equals(BooleanExprs.True.toStaticBooleanExpr())) {
      return simpleTrueStatements;
    } else if (simpleGuard.equals(BooleanExprs.False.toStaticBooleanExpr())) {
      return simpleFalseStatements;
    } else if (simpleTrueStatements.size() == 0 && simpleFalseStatements.size() == 0) {
      return Collections.<Statement>emptyList();
    } else {
      If simple = new If();
      simple.setGuard(simpleGuard);
      simple.setTrueStatements(simpleTrueStatements);
      simple.setFalseStatements(simpleFalseStatements);
      simple.setComment(getComment());
      return Collections.singletonList(simple);
    }
  }
}
