package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that matches when an {@link Environment}'s route has the given {@code color2}
 * value ({@link BgpRoute#getColor2()}). Other routes have {@code color2} 0.
 */
@ParametersAreNonnullByDefault
public final class MatchColor2 extends BooleanExpr {

  private static final String PROP_COLOR2 = "color2";

  private final long _color2;

  @JsonCreator
  private static MatchColor2 create(@JsonProperty(PROP_COLOR2) long color2) {
    return new MatchColor2(color2);
  }

  public MatchColor2(long color2) {
    _color2 = color2;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchColor2(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    long actual;
    if (environment.getUseOutputAttributes()) {
      actual = color2(environment.getOutputRoute());
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      actual = environment.getIntermediateBgpAttributes().getColor2();
    } else {
      actual = color2(environment.getOriginalRoute());
    }
    return new Result(actual == _color2);
  }

  private static long color2(AbstractRouteBuilder<?, ?> route) {
    return route instanceof BgpRoute.Builder<?, ?> b ? b.getColor2() : 0L;
  }

  private static long color2(AbstractRoute route) {
    return route instanceof BgpRoute<?, ?> b ? b.getColor2() : 0L;
  }

  @JsonProperty(PROP_COLOR2)
  public long getColor2() {
    return _color2;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchColor2)) {
      return false;
    }
    MatchColor2 other = (MatchColor2) obj;
    return _color2 == other._color2;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color2);
  }
}
