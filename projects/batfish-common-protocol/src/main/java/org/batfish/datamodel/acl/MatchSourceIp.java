package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.TraceElement;

/** ACL term that matches source IPs. */
public class MatchSourceIp extends AclLineMatchExpr {
  private static final String PROP_IPS = "ips";

  private final IpSpace _ipSpace;

  MatchSourceIp(IpSpace ips, @Nullable TraceElement traceElement) {
    super(traceElement);
    _ipSpace = ips;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchSourceIp(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_ipSpace, ((MatchSourceIp) o)._ipSpace);
  }

  @JsonProperty(PROP_IPS)
  public IpSpace getIps() {
    return _ipSpace;
  }

  @Override
  public int hashCode() {
    return _ipSpace.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_IPS, _ipSpace)
        .add(PROP_TRACE_ELEMENT, getTraceElement())
        .toString();
  }

  @JsonCreator
  private static MatchSourceIp jsonCreator(
      @JsonProperty(PROP_IPS) IpSpace ips,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    return new MatchSourceIp(ips, traceElement);
  }
}
