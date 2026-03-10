package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;

/** An expression that matches a 32-bit signed integer. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IntMatchExpr implements Serializable {

  public abstract <T, U> T accept(IntMatchExprVisitor<T, U> visitor, U arg);

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public abstract int hashCode();
}
