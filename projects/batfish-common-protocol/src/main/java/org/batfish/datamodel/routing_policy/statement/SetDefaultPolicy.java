package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** Set the default policy name to be used on fall through of another {@link RoutingPolicy} */
@ParametersAreNonnullByDefault
public final class SetDefaultPolicy extends Statement {
  private static final String PROP_DEFAULT_POLICY = "defaultPolicy";

  @Nonnull private final String _defaultPolicy;

  @JsonCreator
  private static SetDefaultPolicy jsonCreator(
      @Nullable @JsonProperty(PROP_DEFAULT_POLICY) String defaultPolicy) {
    checkArgument(defaultPolicy != null, "%s must be provided", PROP_DEFAULT_POLICY);
    return new SetDefaultPolicy(defaultPolicy);
  }

  public SetDefaultPolicy(String defaultPolicy) {
    _defaultPolicy = defaultPolicy;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetDefaultPolicy(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetDefaultPolicy)) {
      return false;
    }
    SetDefaultPolicy other = (SetDefaultPolicy) obj;
    return _defaultPolicy.equals(other._defaultPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_defaultPolicy);
  }

  @Override
  public Result execute(Environment environment) {
    environment.setDefaultPolicy(_defaultPolicy);
    return new Result();
  }

  @JsonProperty(PROP_DEFAULT_POLICY)
  @Nonnull
  public String getDefaultPolicy() {
    return _defaultPolicy;
  }
}
