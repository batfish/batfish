package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.LongExpr;

/**
 * Routing policy statement that sets the output route's tag to some default value if the tag has
 * not already been explicitly set according to {@link Environment#getTagExplicitlySet()}.
 */
@ParametersAreNonnullByDefault
public final class SetDefaultTag extends Statement {
  private static final String PROP_TAG = "tag";

  private final @Nonnull LongExpr _tag;

  @JsonCreator
  private static SetDefaultTag jsonCreator(@JsonProperty(PROP_TAG) @Nullable LongExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_TAG);
    return new SetDefaultTag(expr);
  }

  public SetDefaultTag(LongExpr expr) {
    _tag = expr;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetDefaultTag(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetDefaultTag)) {
      return false;
    }
    SetDefaultTag other = (SetDefaultTag) obj;
    return _tag.equals(other._tag);
  }

  @Override
  public Result execute(Environment environment) {
    if (!environment.getTagExplicitlySet()) {
      long tag = _tag.evaluate(environment);
      environment.getOutputRoute().setTag(tag);
      if (environment.getWriteToIntermediateBgpAttributes()) {
        environment.getIntermediateBgpAttributes().setTag(tag);
      }
    }
    return new Result();
  }

  @JsonProperty(PROP_TAG)
  public @Nonnull LongExpr getTag() {
    return _tag;
  }

  @Override
  public int hashCode() {
    return _tag.hashCode();
  }
}
