package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A reference to a named {@link CommunityMatchExpr}. */
public final class CommunityMatchExprReference extends CommunityMatchExpr {

  public CommunityMatchExprReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityMatchExprReference(this, arg);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityMatchExprReference)) {
      return false;
    }
    return _name.equals(((CommunityMatchExprReference) obj)._name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  private static final String PROP_NAME = "name";

  @JsonCreator
  private static @Nonnull CommunityMatchExprReference create(
      @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return new CommunityMatchExprReference(name);
  }

  private final @Nonnull String _name;
}
