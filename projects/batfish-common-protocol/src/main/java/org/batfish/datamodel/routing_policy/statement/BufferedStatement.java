package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public final class BufferedStatement extends Statement {
  private static final String PROP_STATEMENT = "statement";

  @Nonnull private Statement _statement;

  @JsonCreator
  private static BufferedStatement jsonCreator(
      @Nullable @JsonProperty(PROP_STATEMENT) Statement statement) {
    checkArgument(statement != null, "%s is required", PROP_STATEMENT);
    return new BufferedStatement(statement);
  }

  public BufferedStatement(@Nonnull Statement statement) {
    _statement = statement;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitBufferedStatement(this, arg);
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
    if (!(obj instanceof BufferedStatement)) {
      return false;
    }
    BufferedStatement other = (BufferedStatement) obj;
    return _statement.equals(other._statement);
  }

  @Override
  public Result execute(Environment environment) {
    environment.setBuffered(true);
    Result result = _statement.execute(environment);
    return result;
  }

  @JsonProperty(PROP_STATEMENT)
  @Nonnull
  public Statement getStatement() {
    return _statement;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _statement.hashCode();
    return result;
  }

  public void setStatement(@Nonnull Statement statement) {
    _statement = statement;
  }
}
