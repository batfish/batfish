package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** A line of a {@link CommunityAcl}. */
public final class CommunityAclLine implements Serializable {

  public CommunityAclLine(LineAction action, CommunityMatchExpr communityMatchExpr) {
    _action = action;
    _communityMatchExpr = communityMatchExpr;
  }

  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_COMMUNITY_MATCH_EXPR)
  public @Nonnull CommunityMatchExpr getCommunityMatchExpr() {
    return _communityMatchExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityAclLine)) {
      return false;
    }
    CommunityAclLine rhs = (CommunityAclLine) obj;
    return _action == rhs._action && _communityMatchExpr.equals(rhs._communityMatchExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action.ordinal(), _communityMatchExpr);
  }

  private static final String PROP_ACTION = "action";
  private static final String PROP_COMMUNITY_MATCH_EXPR = "communityMatchExpr";

  @JsonCreator
  private static @Nonnull CommunityAclLine create(
      @JsonProperty(PROP_ACTION) @Nullable LineAction action,
      @JsonProperty(PROP_COMMUNITY_MATCH_EXPR) @Nullable CommunityMatchExpr communityMatchExpr) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(communityMatchExpr != null, "Missing %s", PROP_COMMUNITY_MATCH_EXPR);
    return new CommunityAclLine(action, communityMatchExpr);
  }

  private final @Nonnull LineAction _action;
  private final @Nonnull CommunityMatchExpr _communityMatchExpr;
}
