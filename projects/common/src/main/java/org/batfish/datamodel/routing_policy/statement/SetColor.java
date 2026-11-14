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
 * Sets the Junos {@code color} ({@link BgpRoute#getColor()}) of the output route. Has no effect on
 * a route that is not a BGP route.
 */
@ParametersAreNonnullByDefault
public final class SetColor extends Statement {
  private static final String PROP_COLOR = "color";

  private final @Nonnull LongExpr _color;

  @JsonCreator
  private static SetColor jsonCreator(@JsonProperty(PROP_COLOR) @Nullable LongExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_COLOR);
    return new SetColor(expr);
  }

  public SetColor(LongExpr expr) {
    _color = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetColor(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetColor)) {
      return false;
    }
    SetColor other = (SetColor) obj;
    return _color.equals(other._color);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    long color = _color.evaluate(environment);
    if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?> b) {
      b.setColor(color);
    }
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setColor(color);
    }
    return result;
  }

  @JsonProperty(PROP_COLOR)
  public @Nonnull LongExpr getColor() {
    return _color;
  }

  @Override
  public int hashCode() {
    return _color.hashCode();
  }
}
