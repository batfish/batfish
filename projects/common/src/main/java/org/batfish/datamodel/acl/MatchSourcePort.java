package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.TraceElement;

/** ACL term that matches source ports. */
public class MatchSourcePort extends AclLineMatchExpr {
  private static final String PROP_PORTS = "ports";

  private final IntegerSpace _ports;

  MatchSourcePort(IntegerSpace ports, @Nullable TraceElement traceElement) {
    super(traceElement);
    _ports = ports;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSourcePort(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _ports.equals(((MatchSourcePort) o)._ports);
  }

  @JsonProperty(PROP_PORTS)
  public IntegerSpace getPorts() {
    return _ports;
  }

  @Override
  public int hashCode() {
    return _ports.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_PORTS, _ports)
        .add(PROP_TRACE_ELEMENT, getTraceElement())
        .toString();
  }

  @JsonCreator
  private static MatchSourcePort jsonCreator(
      @JsonProperty(PROP_PORTS) IntegerSpace ports,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    return new MatchSourcePort(ports, traceElement);
  }
}
