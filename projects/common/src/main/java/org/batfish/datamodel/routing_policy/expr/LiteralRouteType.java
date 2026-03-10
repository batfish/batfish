package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class LiteralRouteType extends RouteTypeExpr {
  private static final String PROP_TYPE = "type";

  private @Nonnull RouteType _type;

  @JsonCreator
  private static LiteralRouteType jsonCreator(@JsonProperty(PROP_TYPE) @Nullable RouteType type) {
    checkArgument(type != null, "%s must be provided", PROP_TYPE);
    return new LiteralRouteType(type);
  }

  public LiteralRouteType(RouteType type) {
    _type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralRouteType)) {
      return false;
    }
    LiteralRouteType other = (LiteralRouteType) obj;
    return _type == other._type;
  }

  @Override
  public RouteType evaluate(Environment environment) {
    return _type;
  }

  @JsonProperty(PROP_TYPE)
  public @Nonnull RouteType getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.ordinal();
    return result;
  }

  public void setType(RouteType type) {
    _type = type;
  }
}
