package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IsisLevelExpr;

@ParametersAreNonnullByDefault
public final class SetIsisLevel extends Statement {
  private static final String PROP_LEVEL = "level";

  private @Nonnull IsisLevelExpr _level;

  @JsonCreator
  private static SetIsisLevel jsonCreator(@JsonProperty(PROP_LEVEL) @Nullable IsisLevelExpr level) {
    checkArgument(level != null, "%s must be provided", level);
    return new SetIsisLevel(level);
  }

  public SetIsisLevel(IsisLevelExpr level) {
    _level = level;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetIsisLevel(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetIsisLevel)) {
      return false;
    }
    SetIsisLevel other = (SetIsisLevel) obj;
    return _level.equals(other._level);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    IsisLevel level = _level.evaluate(environment);
    IsisRoute.Builder isisRouteBuilder = (IsisRoute.Builder) environment.getOutputRoute();
    isisRouteBuilder.setLevel(level);
    return result;
  }

  @JsonProperty(PROP_LEVEL)
  public @Nonnull IsisLevelExpr getLevel() {
    return _level;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _level.hashCode();
    return result;
  }

  public void setLevel(IsisLevelExpr level) {
    _level = level;
  }
}
