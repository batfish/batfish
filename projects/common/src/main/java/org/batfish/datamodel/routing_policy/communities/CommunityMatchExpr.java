package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * An expression that matches a {@link org.batfish.datamodel.bgp.community.Community} under a given
 * {@link CommunityContext}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CommunityMatchExpr implements Serializable {

  public abstract <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg);

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public abstract int hashCode();
}
