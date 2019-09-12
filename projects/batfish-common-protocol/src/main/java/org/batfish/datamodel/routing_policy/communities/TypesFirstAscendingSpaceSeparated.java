package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link CommunitySetRendering} accomplished by rendering of each constituent community,
 * separated by spaces, sorted first by community type (standard, extended, then large), and then in
 * ascdending order of integer value.
 */
public final class TypesFirstAscendingSpaceSeparated extends CommunitySetRendering {

  public TypesFirstAscendingSpaceSeparated(CommunityRendering communityRendering) {
    super(communityRendering);
  }

  @Override
  public <T, U> T accept(CommunitySetRenderingVisitor<T, U> visitor, U arg) {
    return visitor.visitTypesFirstAscendingSpaceSeparated(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TypesFirstAscendingSpaceSeparated)) {
      return false;
    }
    return _communityRendering.equals(
        ((TypesFirstAscendingSpaceSeparated) obj)._communityRendering);
  }

  @Override
  public int hashCode() {
    return _communityRendering.hashCode();
  }

  @JsonCreator
  private static @Nonnull TypesFirstAscendingSpaceSeparated create(
      @JsonProperty(PROP_COMMUNITY_RENDERING) @Nullable CommunityRendering communityRendering) {
    checkArgument(communityRendering != null, "Missing %s", PROP_COMMUNITY_RENDERING);
    return new TypesFirstAscendingSpaceSeparated(communityRendering);
  }
}
