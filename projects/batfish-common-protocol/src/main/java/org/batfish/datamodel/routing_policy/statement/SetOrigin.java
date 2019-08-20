package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;

/** Set the origin type on a BGP route */
@ParametersAreNonnullByDefault
public final class SetOrigin extends Statement {
  private static final String PROP_ORIGIN_TYPE = "originType";

  @Nonnull private OriginExpr _origin;

  @JsonCreator
  private static SetOrigin jsonCreator(@Nullable @JsonProperty(PROP_ORIGIN_TYPE) OriginExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_ORIGIN_TYPE);
    return new SetOrigin(expr);
  }

  public SetOrigin(OriginExpr origin) {
    _origin = origin;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SetOrigin)) {
      return false;
    }
    SetOrigin other = (SetOrigin) o;
    return _origin.equals(other._origin);
  }

  @Override
  public int hashCode() {
    return _origin.hashCode();
  }

  @Override
  @Nonnull
  public Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>)) {
      // Do nothing for non-BGP routes
      return new Result();
    }
    BgpRoute.Builder<?, ?> bgpRoute = (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
    OriginType originType = _origin.evaluate(environment);
    bgpRoute.setOriginType(originType);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setOriginType(originType);
    }
    return new Result();
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginExpr getOriginType() {
    return _origin;
  }
}
