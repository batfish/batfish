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
import org.batfish.datamodel.routing_policy.expr.IntExpr;

@ParametersAreNonnullByDefault
public final class SetWeight extends Statement {
  private static final String PROP_WEIGHT = "weight";
  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private IntExpr _weight;

  @JsonCreator
  private static SetWeight jsonCreator(@Nullable @JsonProperty(PROP_WEIGHT) IntExpr weight) {
    checkArgument(weight != null, "%s must be provided", PROP_WEIGHT);
    return new SetWeight(weight);
  }

  public SetWeight(IntExpr weight) {
    _weight = weight;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetWeight)) {
      return false;
    }
    SetWeight other = (SetWeight) obj;
    return _weight.equals(other._weight);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    int weight = _weight.evaluate(environment);
    BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment.getOutputRoute();
    bgpRouteBuilder.setWeight(weight);
    return result;
  }

  @JsonProperty(PROP_WEIGHT)
  @Nonnull
  public IntExpr getWeight() {
    return _weight;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _weight.hashCode();
    return result;
  }

  public void setWeight(IntExpr weight) {
    _weight = weight;
  }
}
