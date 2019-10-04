package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;

/** Utility class for constructing {@link CommunitySetExpr}s. */
public final class CommunitySetExprs {

  public static @Nonnull CommunitySetExpr empty() {
    return EMPTY;
  }

  private static final CommunitySetExpr EMPTY = new LiteralCommunitySet(CommunitySet.empty());

  private CommunitySetExprs() {}
}
