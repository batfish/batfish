package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;

@ParametersAreNonnullByDefault
public final class PrependAsPath extends Statement {
  private static final String PROP_EXPR = "expr";

  /** */
  private static final long serialVersionUID = 1L;

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
    List<Long> toPrepend = _expr.evaluate(environment);
    List<SortedSet<Long>> newAsPaths =
        toPrepend.stream().map(ImmutableSortedSet::of).collect(ImmutableList.toImmutableList());

    BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment.getOutputRoute();
    bgpRouteBuilder.setAsPath(
        ImmutableList.<SortedSet<Long>>builder()
            .addAll(newAsPaths)
            .addAll(bgpRouteBuilder.getAsPath())
            .build());

    if (environment.getWriteToIntermediateBgpAttributes()) {
      BgpRoute.Builder ir = environment.getIntermediateBgpAttributes();
      ir.setAsPath(
          ImmutableList.<SortedSet<Long>>builder()
              .addAll(newAsPaths)
              .addAll(ir.getAsPath())
              .build());
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
