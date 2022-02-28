package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
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
public final class AppendAsPath extends Statement {
  private static final String PROP_EXPR = "expr";

  @Nonnull private AsPathListExpr _expr;

  @JsonCreator
  private static AppendAsPath jsonCreator(@Nullable @JsonProperty(PROP_EXPR) AsPathListExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new AppendAsPath(expr);
  }

  public AppendAsPath(AsPathListExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitAppendAsPath(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof AppendAsPath)) {
      return false;
    }
    AppendAsPath other = (AppendAsPath) obj;
    return _expr.equals(other._expr);
  }

  @Override
  public Result execute(Environment environment) {
    if (!(environment.getOutputRoute() instanceof HasWritableAsPath<?, ?>)) {
      return new Result();
    }
    List<Long> toAppend = _expr.evaluate(environment);
    List<AsSet> newAsPaths =
        toAppend.stream().map(AsSet::of).collect(ImmutableList.toImmutableList());

    HasWritableAsPath<?, ?> outputRoute = (HasWritableAsPath<?, ?>) environment.getOutputRoute();
    AsPath inputAsPath =
        environment.getReadFromIntermediateBgpAttributes()
            ? environment.getIntermediateBgpAttributes().getAsPath()
            : outputRoute.getAsPath();
    AsPath newAsPath =
        AsPath.of(
            ImmutableList.<AsSet>builder()
                .addAll(inputAsPath.getAsSets())
                .addAll(newAsPaths)
                .build());
    outputRoute.setAsPath(newAsPath);

    // TODO: Clean up this paradigm. Currently all over we write to both output and intermediate
    //       when write-to-intermediate is true. Using current paradigm.
    if (environment.getWriteToIntermediateBgpAttributes()) {
      BgpRoute.Builder<?, ?> ir = environment.getIntermediateBgpAttributes();
      ir.setAsPath(newAsPath);
    }

    return new Result();
  }

  @JsonProperty(PROP_EXPR)
  @Nonnull
  public AsPathListExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  public void setExpr(AsPathListExpr expr) {
    _expr = expr;
  }
}
