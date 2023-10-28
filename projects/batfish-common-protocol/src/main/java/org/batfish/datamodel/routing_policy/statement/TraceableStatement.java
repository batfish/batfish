package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * A statement to enable tracing. It attaches its traceElement when executed with environment with
 * tracing on and then executes its inner statements in a subtrace.
 */
@ParametersAreNonnullByDefault
public class TraceableStatement extends Statement {
  private static final String PROP_INNER_STATEMENTS = "innerStatements";
  private static final String PROP_TRACE_ELEMENT = "traceElement";

  private final List<Statement> _innerStatements;
  private final TraceElement _traceElement;

  @JsonCreator
  private static TraceableStatement create(
      @JsonProperty(PROP_INNER_STATEMENTS) @Nullable List<Statement> innerStatements,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    checkArgument(traceElement != null, "Trace element cannot be null for TraceableStatement");
    return new TraceableStatement(traceElement, firstNonNull(innerStatements, ImmutableList.of()));
  }

  public TraceableStatement(TraceElement traceElement, List<Statement> innerStatements) {
    _innerStatements = ImmutableList.copyOf(innerStatements);
    _traceElement = traceElement;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass().getSimpleName())
        .add("innerStatement", _innerStatements)
        .add("traceElement", _traceElement)
        .toString();
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitTraceableStatement(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TraceableStatement)) {
      return false;
    }
    TraceableStatement other = (TraceableStatement) obj;
    return Objects.equals(_innerStatements, other._innerStatements)
        && Objects.equals(_traceElement, other._traceElement);
  }

  @Override
  public Result execute(Environment environment) {
    if (environment.getTracer() != null) {
      environment.getTracer().newSubTrace();
      environment.getTracer().setTraceElement(_traceElement);
    }
    try {
      for (Statement statement : _innerStatements) {
        Result result = statement.execute(environment);
        if (result.getExit() || result.getReturn()) {
          return result;
        }
      }
      return Result.builder().setFallThrough(true).build();
    } finally {
      if (environment.getTracer() != null) {
        environment.getTracer().endSubTrace();
      }
    }
  }

  @Override
  public List<Statement> simplify() {
    if (_simplified != null) {
      return _simplified;
    }
    ImmutableList.Builder<Statement> simplifiedInnerStatements = ImmutableList.builder();
    for (Statement innerStatement : _innerStatements) {
      simplifiedInnerStatements.addAll(innerStatement.simplify());
    }
    _simplified =
        ImmutableList.of(new TraceableStatement(_traceElement, simplifiedInnerStatements.build()));
    return _simplified;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _innerStatements.hashCode();
    result = prime * result + _traceElement.hashCode();
    return result;
  }

  @JsonProperty(PROP_INNER_STATEMENTS)
  public List<Statement> getInnerStatements() {
    return _innerStatements;
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  public TraceElement getTraceElement() {
    return _traceElement;
  }
}
