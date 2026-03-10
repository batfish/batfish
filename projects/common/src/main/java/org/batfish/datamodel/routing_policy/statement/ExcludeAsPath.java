package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.HasWritableAsPath;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.AsPathListExpr;

/** Class to represent exclude as-path statement */
@ParametersAreNonnullByDefault
public final class ExcludeAsPath extends Statement {
  private static final String PROP_EXPR = "expr";

  /** Expression that holds the information of the statement */
  private @Nonnull AsPathListExpr _expr;

  @JsonCreator
  private static ExcludeAsPath jsonCreator(@JsonProperty(PROP_EXPR) @Nullable AsPathListExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new ExcludeAsPath(expr);
  }

  public ExcludeAsPath(AsPathListExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitExcludeAsPath(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ExcludeAsPath)) {
      return false;
    }
    ExcludeAsPath other = (ExcludeAsPath) obj;
    return _expr.equals(other._expr);
  }

  @Override
  public Result execute(Environment environment) {
    if ((environment.getOutputRoute() instanceof HasWritableAsPath<?, ?>)) {
      List<Long> asnsToExclude = new ArrayList<>(_expr.evaluate(environment));

      HasWritableAsPath<?, ?> outputRoute = (HasWritableAsPath<?, ?>) environment.getOutputRoute();
      outputRoute.setAsPath(outputRoute.getAsPath().removeASNs(asnsToExclude));

      if (environment.getWriteToIntermediateBgpAttributes()) {
        BgpRoute.Builder<?, ?> ir = environment.getIntermediateBgpAttributes();
        ir.setAsPath(ir.getAsPath().removeASNs(asnsToExclude));
      }
    }

    return new Result();
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull AsPathListExpr getExpr() {
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
