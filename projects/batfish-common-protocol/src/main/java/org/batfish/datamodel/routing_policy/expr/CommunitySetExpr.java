package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.SortedSet;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class CommunitySetExpr implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  public abstract SortedSet<Long> communities(Environment environment);

  public abstract SortedSet<Long> communities(
      Environment environment, SortedSet<Long> communityCandidates);

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  /** Return true iff this {@link CommunitySetExpr} matches any community in the given set. */
  public abstract boolean matchSingleCommunity(
      Environment environment, SortedSet<Long> communities);
}
