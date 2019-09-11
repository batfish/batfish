package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * An expression representing a {@link CommunitySet} dependent on the evaluation {@link
 * CommunityContext}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CommunitySetExpr implements Serializable {

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public abstract int hashCode();

  protected abstract <T> T accept(CommunitySetExprVisitor<T> visitor);
}
