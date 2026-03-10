package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HasWritableOriginType;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;

/** Set the origin type on a BGP route */
@ParametersAreNonnullByDefault
public final class SetOrigin extends Statement {
  private static final String PROP_ORIGIN_TYPE = "originType";

  private @Nonnull OriginExpr _origin;

  @JsonCreator
  private static SetOrigin jsonCreator(@JsonProperty(PROP_ORIGIN_TYPE) @Nullable OriginExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_ORIGIN_TYPE);
    return new SetOrigin(expr);
  }

  public SetOrigin(OriginExpr origin) {
    _origin = origin;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetOrigin(this, arg);
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
  public @Nonnull Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof HasWritableOriginType<?, ?>)) {
      // Do nothing for routes without origin type
      return new Result();
    }
    HasWritableOriginType<?, ?> outputRoute =
        (HasWritableOriginType<?, ?>) environment.getOutputRoute();
    OriginType originType = _origin.evaluate(environment);
    outputRoute.setOriginType(originType);
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
