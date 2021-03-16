package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.HasWritableAsPath;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;

@ParametersAreNonnullByDefault
public final class PrependAsPath extends Statement {
  private static final String PROP_EXPR = "expr";

  @Nonnull private AsPathListExpr _expr;

  @JsonCreator
  private static PrependAsPath jsonCreator(@Nullable @JsonProperty(PROP_EXPR) AsPathListExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new PrependAsPath(expr);
  }

  public PrependAsPath(AsPathListExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitPrependAsPath(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof PrependAsPath)) {
      return false;
    }
    PrependAsPath other = (PrependAsPath) obj;
    return _expr.equals(other._expr);
  }

  @Override
  public Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof HasWritableAsPath<?, ?>)) {
      return new Result();
    }
    List<Long> toPrepend = _expr.evaluate(environment);
    List<AsSet> newAsPaths = toPrepend.stream().map(AsSet::of).collect(Collectors.toList());

    HasWritableAsPath<?, ?> outputRoute = (HasWritableAsPath<?, ?>) environment.getOutputRoute();
    outputRoute.setAsPath(
        AsPath.of(
            ImmutableList.<AsSet>builder()
                .addAll(newAsPaths)
                .addAll(outputRoute.getAsPath().getAsSets())
                .build()));

    if (environment.getWriteToIntermediateBgpAttributes()) {
      BgpRoute.Builder<?, ?> ir = environment.getIntermediateBgpAttributes();
      ir.setAsPath(
          AsPath.of(
              ImmutableList.<AsSet>builder()
                  .addAll(newAsPaths)
                  .addAll(ir.getAsPath().getAsSets())
                  .build()));
    }

    Result result = new Result();
    return result;
  }

  @JsonProperty(PROP_EXPR)
  @Nonnull
  public AsPathListExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expr.hashCode();
    return result;
  }

  public void setExpr(AsPathListExpr expr) {
    _expr = expr;
  }
}
