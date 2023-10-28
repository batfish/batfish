package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class VarAsPathSet extends AsPathSetExpr {
  private static final String PROP_VAR = "var";

  private @Nonnull String _var;

  @JsonCreator
  private static VarAsPathSet jsonCreator(@JsonProperty(PROP_VAR) @Nullable String var) {
    checkArgument(var != null, "%s must be provided", PROP_VAR);
    return new VarAsPathSet(var);
  }

  public VarAsPathSet(String var) {
    _var = var;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof VarAsPathSet)) {
      return false;
    }
    VarAsPathSet other = (VarAsPathSet) obj;
    return _var.equals(other._var);
  }

  @JsonProperty(PROP_VAR)
  public @Nonnull String getVar() {
    return _var;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _var.hashCode();
    return result;
  }

  @Override
  public boolean matches(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public void setVar(String var) {
    _var = var;
  }
}
