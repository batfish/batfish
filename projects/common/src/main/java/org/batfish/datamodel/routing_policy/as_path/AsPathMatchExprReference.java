package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A referenced to a named {@link AsPathMatchExpr}. */
public final class AsPathMatchExprReference extends AsPathMatchExpr {

  public static @Nonnull AsPathMatchExprReference of(String name) {
    return new AsPathMatchExprReference(name);
  }

  @Override
  public <T, U> T accept(AsPathMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAsPathMatchExprReference(this, arg);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AsPathMatchExprReference)) {
      return false;
    }
    AsPathMatchExprReference that = (AsPathMatchExprReference) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  private static final String PROP_NAME = "name";

  @JsonCreator
  private static @Nonnull AsPathMatchExprReference create(
      @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return of(name);
  }

  private final @Nonnull String _name;

  private AsPathMatchExprReference(String name) {
    _name = name;
  }
}
