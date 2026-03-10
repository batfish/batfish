package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a {@link CommunitySet} if it is matched by the provided regex using the provided
 * rendering.
 */
public final class CommunitySetMatchRegex extends CommunitySetMatchExpr {

  public CommunitySetMatchRegex(CommunitySetRendering communitySetRendering, String regex) {
    _communitySetRendering = communitySetRendering;
    _regex = regex;
  }

  @JsonProperty(PROP_COMMUNITY_SET_RENDERING)
  public @Nonnull CommunitySetRendering getCommunitySetRendering() {
    return _communitySetRendering;
  }

  @JsonProperty(PROP_REGEX)
  public @Nonnull String getRegex() {
    return _regex;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetMatchRegex)) {
      return false;
    }
    CommunitySetMatchRegex rhs = (CommunitySetMatchRegex) obj;
    return _communitySetRendering.equals(rhs._communitySetRendering) && _regex.equals(rhs._regex);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = 31 * _communitySetRendering.hashCode() + _regex.hashCode();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public <T, U> T accept(CommunitySetMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetMatchRegex(this, arg);
  }

  private static final String PROP_COMMUNITY_SET_RENDERING = "communityRendering";
  private static final String PROP_REGEX = "regex";

  @JsonCreator
  private static @Nonnull CommunitySetMatchRegex create(
      @JsonProperty(PROP_COMMUNITY_SET_RENDERING) @Nullable
          CommunitySetRendering communitySetRendering,
      @JsonProperty(PROP_REGEX) @Nullable String regex) {
    checkArgument(communitySetRendering != null, "Missing %s", PROP_COMMUNITY_SET_RENDERING);
    checkArgument(regex != null, "Missing %s", PROP_REGEX);
    return new CommunitySetMatchRegex(communitySetRendering, regex);
  }

  private final @Nonnull CommunitySetRendering _communitySetRendering;
  private final @Nonnull String _regex;
  private transient int _hashCode; // cached hash
}
