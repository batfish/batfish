package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchRouteType extends BooleanExpr {
  private static final String PROP_TYPE = "type";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private RouteTypeExpr _type;

  @JsonCreator
  private static MatchRouteType jsonCreator(@Nullable @JsonProperty(PROP_TYPE) RouteTypeExpr type) {
    checkArgument(type != null, "%s must be provided", PROP_TYPE);
    return new MatchRouteType(type);
  }

  public MatchRouteType(RouteTypeExpr type) {
    _type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchRouteType)) {
      return false;
    }
    MatchRouteType other = (MatchRouteType) obj;
    return _type.equals(other._type);
  }

  @Override
  public Result evaluate(Environment environment) {
    RouteType type = _type.evaluate(environment);
    throw new BatfishException("unimplemented: match route type: " + type.routeTypeName());
  }

  @JsonProperty(PROP_TYPE)
  @Nonnull
  public RouteTypeExpr getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.hashCode();
    return result;
  }

  public void setType(RouteTypeExpr type) {
    _type = type;
  }
}
