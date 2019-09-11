package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;

/** An expression that matches a {@link Community} under a given {@link CommunityContext}. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class CommunityMatchExpr implements Serializable {

  public abstract boolean equals(@Nullable Object obj);

  public abstract int hashCode();

  protected abstract <T> T accept(CommunityMatchExprVisitor<T> visitor);
}
