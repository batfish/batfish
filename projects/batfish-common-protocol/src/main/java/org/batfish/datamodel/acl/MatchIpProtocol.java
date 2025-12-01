package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;

/** ACL term that matches IP protocols. */
public class MatchIpProtocol extends AclLineMatchExpr {
  private static final String PROP_PROTOCOL = "protocol";

  private final IpProtocol _protocol;

  MatchIpProtocol(IpProtocol protocol, @Nullable TraceElement traceElement) {
    super(traceElement);
    _protocol = protocol;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchIpProtocol(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _protocol == ((MatchIpProtocol) o)._protocol;
  }

  @JsonProperty(PROP_PROTOCOL)
  public IpProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public int hashCode() {
    return _protocol.ordinal();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_PROTOCOL, _protocol)
        .add(PROP_TRACE_ELEMENT, getTraceElement())
        .toString();
  }

  @JsonCreator
  private static MatchIpProtocol jsonCreator(
      @JsonProperty(PROP_PROTOCOL) IpProtocol protocol,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    return new MatchIpProtocol(protocol, traceElement);
  }
}
