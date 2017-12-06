package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class BufferedStatement extends Statement {

  /** */
  private static final long serialVersionUID = 1L;

  private Statement _statement;

  @JsonCreator
  private BufferedStatement() {}

  public BufferedStatement(Statement statement) {
    _statement = statement;
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    return _statement.collectSources(parentSources, routingPolicies, w);
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
    BufferedStatement other = (BufferedStatement) obj;
    if (_statement == null) {
      if (other._statement != null) {
        return false;
      }
    } else if (!_statement.equals(other._statement)) {
      return false;
    }
    return true;
  }

  @Override
  public Result execute(Environment environment) {
    environment.setBuffered(true);
    Result result = _statement.execute(environment);
    return result;
  }

  public Statement getStatement() {
    return _statement;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_statement == null) ? 0 : _statement.hashCode());
    return result;
  }

  public void setStatement(Statement statement) {
    _statement = statement;
  }
}
