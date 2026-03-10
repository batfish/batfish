package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HasWritableWeight;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

@ParametersAreNonnullByDefault
public final class SetWeight extends Statement {
  private static final String PROP_WEIGHT = "weight";

  private @Nonnull IntExpr _weight;

  @JsonCreator
  private static SetWeight jsonCreator(@JsonProperty(PROP_WEIGHT) @Nullable IntExpr weight) {
    checkArgument(weight != null, "%s must be provided", PROP_WEIGHT);
    return new SetWeight(weight);
  }

  public SetWeight(IntExpr weight) {
    _weight = weight;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetWeight(this, arg);
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
    if (!(environment.getOutputRoute() instanceof HasWritableWeight)) {
      return new Result();
    }
    int weight = _weight.evaluate(environment);
    HasWritableWeight<?, ?> outputRoute = (HasWritableWeight) environment.getOutputRoute();
    outputRoute.setWeight(weight);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setWeight(weight);
    }
    return new Result();
  }

  @JsonProperty(PROP_WEIGHT)
  public @Nonnull IntExpr getWeight() {
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
