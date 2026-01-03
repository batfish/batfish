package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Matches a {@link Community} if it is matched by the provided regex using the provided rendering.
 */
public final class CommunityMatchRegex extends CommunityMatchExpr {

  public CommunityMatchRegex(CommunityRendering communityRendering, String regex) {
    _communityRendering = communityRendering;
    _regex = regex;
  }

  @JsonProperty(PROP_COMMUNITY_RENDERING)
  public @Nonnull CommunityRendering getCommunityRendering() {
    return _communityRendering;
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
    if (!(obj instanceof CommunityMatchRegex)) {
      return false;
    }
    CommunityMatchRegex rhs = (CommunityMatchRegex) obj;
    return _communityRendering.equals(rhs._communityRendering) && _regex.equals(rhs._regex);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = 31 * _communityRendering.hashCode() + _regex.hashCode();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityMatchRegex(this, arg);
  }

  private static final String PROP_COMMUNITY_RENDERING = "communityRendering";
  private static final String PROP_REGEX = "regex";

  @JsonCreator
  private static @Nonnull CommunityMatchRegex create(
      @JsonProperty(PROP_COMMUNITY_RENDERING) @Nullable CommunityRendering communityRendering,
      @JsonProperty(PROP_REGEX) @Nullable String regex) {
    checkArgument(communityRendering != null, "Missing %s", PROP_COMMUNITY_RENDERING);
    checkArgument(regex != null, "Missing %s", PROP_REGEX);
    return new CommunityMatchRegex(communityRendering, regex);
  }

  private final @Nonnull CommunityRendering _communityRendering;
  private final @Nonnull String _regex;
  private transient int _hashCode; // cached hash
}
