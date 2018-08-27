package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class SetDefaultPolicy extends Statement {
  private static final String PROP_DEFAULT_POLICY = "defaultPolicy";

  /** */
  private static final long serialVersionUID = 1L;

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
  public Result execute(Environment environment) {
    environment.setDefaultPolicy(_defaultPolicy);
    Result result = new Result();
    return result;
  }

  @JsonProperty(PROP_DEFAULT_POLICY)
  @Nonnull
  public String getDefaultPolicy() {
    return _defaultPolicy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _defaultPolicy.hashCode();
    return result;
  }
}
