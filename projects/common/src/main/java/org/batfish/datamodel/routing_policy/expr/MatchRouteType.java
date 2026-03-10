package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchRouteType extends BooleanExpr {
  private static final String PROP_TYPE = "type";

  private final @Nonnull RouteTypeExpr _type;

  @JsonCreator
  private static MatchRouteType jsonCreator(@JsonProperty(PROP_TYPE) @Nullable RouteTypeExpr type) {
    checkArgument(type != null, "%s must be provided", PROP_TYPE);
    return new MatchRouteType(type);
  }

  public MatchRouteType(RouteTypeExpr type) {
    _type = type;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchRouteType(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    RouteType type = _type.evaluate(environment);
    throw new BatfishException("Unimplemented: match route type: " + type.routeTypeName());
  }

  @JsonProperty(PROP_TYPE)
  public @Nonnull RouteTypeExpr getType() {
    return _type;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchRouteType)) {
      return false;
    }
    MatchRouteType other = (MatchRouteType) obj;
    return Objects.equals(_type, other._type);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_type);
  }
}
