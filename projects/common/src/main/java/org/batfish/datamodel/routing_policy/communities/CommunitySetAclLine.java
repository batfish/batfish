package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** A line of a {@link CommunitySetAcl}. */
public final class CommunitySetAclLine implements Serializable {

  public CommunitySetAclLine(LineAction action, CommunitySetMatchExpr communitySetMatchExpr) {
    _action = action;
    _communitySetMatchExpr = communitySetMatchExpr;
  }

  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_COMMUNITY_SET_MATCH_EXPR)
  public @Nonnull CommunitySetMatchExpr getCommunitySetMatchExpr() {
    return _communitySetMatchExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetAclLine)) {
      return false;
    }
    CommunitySetAclLine rhs = (CommunitySetAclLine) obj;
    return _action == rhs._action && _communitySetMatchExpr.equals(rhs._communitySetMatchExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action.ordinal(), _communitySetMatchExpr);
  }

  private static final String PROP_ACTION = "action";
  private static final String PROP_COMMUNITY_SET_MATCH_EXPR = "communitySetMatchExpr";

  @JsonCreator
  private static @Nonnull CommunitySetAclLine create(
      @JsonProperty(PROP_ACTION) @Nullable LineAction action,
      @JsonProperty(PROP_COMMUNITY_SET_MATCH_EXPR) @Nullable
          CommunitySetMatchExpr communitySetMatchExpr) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(communitySetMatchExpr != null, "Missing %s", PROP_COMMUNITY_SET_MATCH_EXPR);
    return new CommunitySetAclLine(action, communitySetMatchExpr);
  }

  private final @Nonnull LineAction _action;
  private final @Nonnull CommunitySetMatchExpr _communitySetMatchExpr;
}
