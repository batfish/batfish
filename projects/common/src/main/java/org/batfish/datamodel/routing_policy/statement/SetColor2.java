package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

/**
 * Sets the Junos {@code color2} ({@link BgpRoute#getColor2()}) of the output route. Has no effect
 * on a route that is not a BGP route.
 */
@ParametersAreNonnullByDefault
public final class SetColor2 extends Statement {
  private static final String PROP_COLOR2 = "color2";

  private final @Nonnull LongExpr _color2;

  @JsonCreator
  private static SetColor2 jsonCreator(@JsonProperty(PROP_COLOR2) @Nullable LongExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_COLOR2);
    return new SetColor2(expr);
  }

  public SetColor2(LongExpr expr) {
    _color2 = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetColor2(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetColor2)) {
      return false;
    }
    SetColor2 other = (SetColor2) obj;
    return _color2.equals(other._color2);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    long color2 = _color2.evaluate(environment);
    if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?> b) {
      b.setColor2(color2);
    }
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setColor2(color2);
    }
    return result;
  }

  @JsonProperty(PROP_COLOR2)
  public @Nonnull LongExpr getColor2() {
    return _color2;
  }

  @Override
  public int hashCode() {
    return _color2.hashCode();
  }
}
