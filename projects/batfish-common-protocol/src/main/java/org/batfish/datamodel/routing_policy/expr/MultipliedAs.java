package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class MultipliedAs extends AsPathListExpr {
  private static final String PROP_EXPR = "expr";
  private static final String PROP_NUMBER = "number";

  private final @Nonnull AsExpr _expr;
  private final @Nonnull IntExpr _number;

  @JsonCreator
  private static MultipliedAs jsonCreator(
      @JsonProperty(PROP_EXPR) @Nullable AsExpr expr,
      @JsonProperty(PROP_NUMBER) @Nullable IntExpr number) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    checkArgument(number != null, "%s must be provided", PROP_NUMBER);
    return new MultipliedAs(expr, number);
  }

  public MultipliedAs(AsExpr expr, IntExpr number) {
    _expr = expr;
    _number = number;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MultipliedAs)) {
      return false;
    }
    MultipliedAs other = (MultipliedAs) obj;
    return _expr.equals(other._expr) && _number.equals(other._number);
  }

  @Override
  public List<Long> evaluate(Environment environment) {
    ImmutableList.Builder<Long> listBuilder = ImmutableList.builder();
    long as = _expr.evaluate(environment);
    int number = _number.evaluate(environment);
    for (int i = 0; i < number; i++) {
      listBuilder.add(as);
    }
    return listBuilder.build();
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull AsExpr getExpr() {
    return _expr;
  }

  @JsonProperty(PROP_NUMBER)
  public @Nonnull IntExpr getNumber() {
    return _number;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expr.hashCode();
    result = prime * result + _number.hashCode();
    return result;
  }
}
