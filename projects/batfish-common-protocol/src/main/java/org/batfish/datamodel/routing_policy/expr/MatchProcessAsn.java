package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has an EIGRP route with an ASN
 * equal to some given ASN.
 */
public final class MatchProcessAsn extends BooleanExpr {

  private static final String PROP_PROCESS_ASN = "asn";

  private final long[] _asn;

  @JsonCreator
  public MatchProcessAsn(@JsonProperty(PROP_PROCESS_ASN) long asn) {
    _asn = new long[] {asn};
  }

  public MatchProcessAsn(long[] asns) {
    _asn = asns;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchProcessAsn(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    if (!(environment.getOriginalRoute() instanceof EigrpRoute)) {
      return new Result(false);
    }
    EigrpRoute route = (EigrpRoute) environment.getOriginalRoute();
    return new Result(Arrays.stream(_asn).anyMatch(s -> s == route.getProcessAsn()));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchProcessAsn)) {
      return false;
    }
    return _asn == ((MatchProcessAsn) obj)._asn;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_asn);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + Arrays.toString(_asn) + ">";
  }
}
