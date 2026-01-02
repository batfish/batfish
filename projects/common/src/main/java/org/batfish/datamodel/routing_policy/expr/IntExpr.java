package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IntExpr implements Serializable {

  public abstract <T, U> T accept(IntExprVisitor<T, U> visitor, U arg);

  @Override
  public abstract boolean equals(Object obj);

  public abstract int evaluate(Environment environment);

  @Override
  public abstract int hashCode();
}
