package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/** A statement that will be traced when executed */
@ParametersAreNonnullByDefault
public class TraceableStatement extends Statement {
  private static final String PROP_INNER_STATEMENT = "innerStatement";
  private static final String PROP_TRACE_ELEMENT = "traceElement";

  private final Statement _innerStatement;
  private final TraceElement _traceElement;

  @JsonCreator
  private static TraceableStatement create(
      @Nullable @JsonProperty(PROP_INNER_STATEMENT) Statement innerStatement,
      @Nullable @JsonProperty(PROP_TRACE_ELEMENT) TraceElement traceElement) {
    checkArgument(innerStatement != null, "Inner statement cannot be null for TraceableStatement");
    checkArgument(traceElement != null, "Trace element cannot be null for TraceableStatement");
    return new TraceableStatement(innerStatement, traceElement);
  }

  public TraceableStatement(Statement innerStatement, TraceElement traceElement) {
    _innerStatement = innerStatement;
    _traceElement = traceElement;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass().getSimpleName())
        .add("innerStatement", _innerStatement)
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
    return Objects.equals(_innerStatement, other._innerStatement)
        && Objects.equals(_traceElement, other._traceElement);
  }

  @Override
  public Result execute(Environment environment) {
    if (environment.getTracer() != null) {
      // this isn't quite right
      environment.getTracer().newSubTrace();
      environment.getTracer().setTraceElement(_traceElement);
    }
    Result result = _innerStatement.execute(environment);
    if (environment.getTracer() != null) {
      environment.getTracer().endSubTrace();
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _innerStatement.hashCode();
    result = prime * result + _traceElement.hashCode();
    return result;
  }

  @JsonProperty(PROP_INNER_STATEMENT)
  public Statement getInnerStatement() {
    return _innerStatement;
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  public TraceElement getTraceElement() {
    return _traceElement;
  }
}
