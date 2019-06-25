package org.batfish.datamodel.routing_policy.expr;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A match expression for a 16-bit half of a community that matches when the half is matched by the
 * contained variable match expression.
 */
public class VarCommunityHalf implements CommunityHalfExpr {
  private static final String PROP_VAR = "var";

  @JsonCreator
  private static @Nonnull VarCommunityHalf create(@JsonProperty(PROP_VAR) String var) {
    return new VarCommunityHalf(requireNonNull(var));
  }

  private final String _var;

  public VarCommunityHalf(@Nonnull String var) {
    _var = var;
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return true;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VarCommunityHalf)) {
      return false;
    }
    return _var.equals(((VarCommunityHalf) obj)._var);
  }

  @JsonProperty(PROP_VAR)
  public @Nonnull String getVar() {
    return _var;
  }

  @Override
  public int hashCode() {
    return _var.hashCode();
  }

  @Override
  public boolean matches(int communityHalf) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }
}
