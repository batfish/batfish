package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HasWritableOriginatorIp;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IpExpr;

/**
 * A {@link Statement} that sets the originator IP of a route that may have one, or else does
 * nothing.
 */
@ParametersAreNonnullByDefault
public final class SetOriginatorIp extends Statement {

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetOriginatorIp(this, arg);
  }

  @Override
  public Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof HasWritableOriginatorIp)) {
      return new Result();
    }
    Ip originatorIp = _originatorIpExpr.evaluate(environment);
    HasWritableOriginatorIp<?, ?> outputRoute =
        (HasWritableOriginatorIp<?, ?>) environment.getOutputRoute();
    outputRoute.setOriginatorIp(originatorIp);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setOriginatorIp(originatorIp);
    }
    return new Result();
  }

  public static @Nonnull SetOriginatorIp of(IpExpr originatorIpExpr) {
    return new SetOriginatorIp(originatorIpExpr);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetOriginatorIp)) {
      return false;
    }
    return _originatorIpExpr.equals(((SetOriginatorIp) obj)._originatorIpExpr);
  }

  @Override
  public int hashCode() {
    return _originatorIpExpr.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add(PROP_ORIGINATOR_IP_EXPR, _originatorIpExpr).toString();
  }

  @JsonCreator
  private static @Nonnull SetOriginatorIp create(
      @JsonProperty(PROP_ORIGINATOR_IP_EXPR) @Nullable IpExpr originatorIpExpr) {
    checkArgument(originatorIpExpr != null, "Missing %s", PROP_ORIGINATOR_IP_EXPR);
    return of(originatorIpExpr);
  }

  private SetOriginatorIp(IpExpr originatorIpExpr) {
    _originatorIpExpr = originatorIpExpr;
  }

  public @Nonnull IpExpr getOriginatorIpExpr() {
    return _originatorIpExpr;
  }

  private static final String PROP_ORIGINATOR_IP_EXPR = "originatorIpExpr";
  private final @Nonnull IpExpr _originatorIpExpr;
}
