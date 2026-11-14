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
 * Boolean expression that matches when an {@link Environment}'s route has the given {@code color}
 * value ({@link BgpRoute#getColor()}). Other routes have {@code color} 0.
 */
@ParametersAreNonnullByDefault
public final class MatchColor extends BooleanExpr {

  private static final String PROP_COLOR = "color";

  private final long _color;

  @JsonCreator
  private static MatchColor create(@JsonProperty(PROP_COLOR) long color) {
    return new MatchColor(color);
  }

  public MatchColor(long color) {
    _color = color;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchColor(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    long actual;
    if (environment.getUseOutputAttributes()) {
      actual = color(environment.getOutputRoute());
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      actual = environment.getIntermediateBgpAttributes().getColor();
    } else {
      actual = color(environment.getOriginalRoute());
    }
    return new Result(actual == _color);
  }

  private static long color(AbstractRouteBuilder<?, ?> route) {
    return route instanceof BgpRoute.Builder<?, ?> b ? b.getColor() : 0L;
  }

  private static long color(AbstractRoute route) {
    return route instanceof BgpRoute<?, ?> b ? b.getColor() : 0L;
  }

  @JsonProperty(PROP_COLOR)
  public long getColor() {
    return _color;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchColor)) {
      return false;
    }
    MatchColor other = (MatchColor) obj;
    return _color == other._color;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color);
  }
}
