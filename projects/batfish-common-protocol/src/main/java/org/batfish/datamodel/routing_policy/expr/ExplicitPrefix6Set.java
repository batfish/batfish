package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class ExplicitPrefix6Set extends Prefix6SetExpr {
  private static final String PROP_PREFIX6_SPACE = "prefix6Space";

  @Nonnull private final Prefix6Space _prefix6Space;

  @JsonCreator
  private static ExplicitPrefix6Set jsonCreator(
      @Nullable @JsonProperty(PROP_PREFIX6_SPACE) Prefix6Space prefix6Space) {
    checkArgument(prefix6Space != null, "%s must be provided", PROP_PREFIX6_SPACE);
    return new ExplicitPrefix6Set(prefix6Space);
  }

  public ExplicitPrefix6Set(Prefix6Space prefix6Space) {
    _prefix6Space = prefix6Space;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ExplicitPrefix6Set)) {
      return false;
    }
    ExplicitPrefix6Set other = (ExplicitPrefix6Set) obj;
    return _prefix6Space.equals(other._prefix6Space);
  }

  @JsonProperty(PROP_PREFIX6_SPACE)
  @Nonnull
  public Prefix6Space getPrefix6Space() {
    return _prefix6Space;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _prefix6Space.hashCode();
    return result;
  }

  @Override
  public boolean matches(Prefix6 prefix6, Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }
}
