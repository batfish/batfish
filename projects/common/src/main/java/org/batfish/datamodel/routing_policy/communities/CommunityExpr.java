package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** An expression representing a {@link org.batfish.datamodel.bgp.community.Community}. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CommunityExpr implements Serializable {

  public abstract <T, U> T accept(CommunityExprVisitor<T, U> visitor, U arg);

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();
}
