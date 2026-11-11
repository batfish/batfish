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
 * Sets the second tag ({@link BgpRoute#getTag2()}) of the output route. Has no effect on a route
 * that is not a BGP route.
 */
@ParametersAreNonnullByDefault
public final class SetTag2 extends Statement {
  private static final String PROP_TAG2 = "tag2";

  private final @Nonnull LongExpr _tag2;

  @JsonCreator
  private static SetTag2 jsonCreator(@JsonProperty(PROP_TAG2) @Nullable LongExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_TAG2);
    return new SetTag2(expr);
  }

  public SetTag2(LongExpr expr) {
    _tag2 = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetTag2(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetTag2)) {
      return false;
    }
    SetTag2 other = (SetTag2) obj;
    return _tag2.equals(other._tag2);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    long tag2 = _tag2.evaluate(environment);
    if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?> b) {
      b.setTag2(tag2);
    }
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setTag2(tag2);
    }
    return result;
  }

  @JsonProperty(PROP_TAG2)
  public @Nonnull LongExpr getTag2() {
    return _tag2;
  }

  @Override
  public int hashCode() {
    return _tag2.hashCode();
  }
}
