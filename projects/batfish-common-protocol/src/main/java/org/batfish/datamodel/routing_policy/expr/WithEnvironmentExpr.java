package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class WithEnvironmentExpr extends BooleanExpr {

  private static final String PROP_EXPR = "expr";
  private static final String PROP_POST_STATEMENTS = "postStatements";
  private static final String PROP_POST_TRUE_STATEMENTS = "postTrueStatements";
  private static final String PROP_PRE_STATEMENTS = "preStatements";

  /** */
  private static final long serialVersionUID = 1L;

  private BooleanExpr _expr;

  private List<Statement> _postStatements;

  private List<Statement> _postTrueStatements;

  private List<Statement> _preStatements;

  public WithEnvironmentExpr() {
    _preStatements = new ArrayList<>();
    _postStatements = new ArrayList<>();
    _postTrueStatements = new ArrayList<>();
  }

  @Override
  public Set<String> collectSources(
      Set<String> sources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    ImmutableSet.Builder<String> childSources = ImmutableSet.builder();
    if (_expr != null) {
      childSources.addAll(_expr.collectSources(sources, routingPolicies, w));
    }
    for (Statement statement : _postStatements) {
      childSources.addAll(statement.collectSources(sources, routingPolicies, w));
    }
    for (Statement statement : _postTrueStatements) {
      childSources.addAll(statement.collectSources(sources, routingPolicies, w));
    }
    for (Statement statement : _preStatements) {
      childSources.addAll(statement.collectSources(sources, routingPolicies, w));
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
    WithEnvironmentExpr other = (WithEnvironmentExpr) obj;
    if (_expr == null) {
      if (other._expr != null) {
        return false;
      }
    } else if (!_expr.equals(other._expr)) {
      return false;
    }
    if (_postStatements == null) {
      if (other._postStatements != null) {
        return false;
      }
    } else if (!_postStatements.equals(other._postStatements)) {
      return false;
    }
    if (_postTrueStatements == null) {
      if (other._postTrueStatements != null) {
        return false;
      }
    } else if (!_postTrueStatements.equals(other._postTrueStatements)) {
      return false;
    }
    if (_preStatements == null) {
      if (other._preStatements != null) {
        return false;
      }
    } else if (!_preStatements.equals(other._preStatements)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    for (Statement statement : _preStatements) {
      statement.execute(environment);
    }
    Result result = _expr.evaluate(environment);
    for (Statement statement : _postStatements) {
      statement.execute(environment);
    }
    if (result.getBooleanValue()) {
      for (Statement statement : _postTrueStatements) {
        statement.execute(environment);
      }
    }
    return result;
  }

  @JsonProperty(PROP_EXPR)
  public BooleanExpr getExpr() {
    return _expr;
  }

  @JsonProperty(PROP_POST_STATEMENTS)
  public List<Statement> getPostStatements() {
    return _postStatements;
  }

  @JsonProperty(PROP_POST_TRUE_STATEMENTS)
  public List<Statement> getPostTrueStatements() {
    return _postTrueStatements;
  }

  @JsonProperty(PROP_PRE_STATEMENTS)
  public List<Statement> getPreStatements() {
    return _preStatements;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expr == null) ? 0 : _expr.hashCode());
    result = prime * result + ((_postStatements == null) ? 0 : _postStatements.hashCode());
    result = prime * result + ((_postTrueStatements == null) ? 0 : _postTrueStatements.hashCode());
    result = prime * result + ((_preStatements == null) ? 0 : _preStatements.hashCode());
    return result;
  }

  @JsonProperty(PROP_EXPR)
  public void setExpr(BooleanExpr expr) {
    _expr = expr;
  }

  @JsonProperty(PROP_POST_STATEMENTS)
  public void setPostStatements(List<Statement> postStatements) {
    _postStatements = postStatements;
  }

  @JsonProperty(PROP_POST_TRUE_STATEMENTS)
  public void setPostTrueStatements(List<Statement> postTrueStatements) {
    _postTrueStatements = postTrueStatements;
  }

  @JsonProperty(PROP_PRE_STATEMENTS)
  public void setPreStatements(List<Statement> preStatements) {
    _preStatements = preStatements;
  }
}
