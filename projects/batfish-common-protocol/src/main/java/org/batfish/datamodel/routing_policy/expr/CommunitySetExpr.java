package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CommunitySetExpr implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  /** Returns the set of all communities that match this {@link CommunitySetExpr}. */
  public abstract SortedSet<Long> allCommunities(Environment environment);

  /** Returns the subset of the given communities that match this {@link CommunitySetExpr}. */
  public abstract SortedSet<Long> communities(
      Environment environment, Set<Long> communityCandidates);

  /** Return true iff this {@link CommunitySetExpr} matches any community in the given set. */
  public final boolean matchSingleCommunity(Environment environment, Set<Long> communities) {
    return !this.communities(environment, communities).isEmpty();
  }

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();
}
