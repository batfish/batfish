package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A referenced to a named {@link AsPathExpr}. */
public final class AsPathExprReference extends AsPathExpr {

  public static @Nonnull AsPathExprReference of(String name) {
    return new AsPathExprReference(name);
  }

  @Override
  public <T, U> T accept(AsPathExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAsPathExprReference(this, arg);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AsPathExprReference)) {
      return false;
    }
    AsPathExprReference that = (AsPathExprReference) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  private static final String PROP_NAME = "name";

  @JsonCreator
  private static @Nonnull AsPathExprReference create(
      @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return of(name);
  }

  private final @Nonnull String _name;

  private AsPathExprReference(String name) {
    _name = name;
  }
}
