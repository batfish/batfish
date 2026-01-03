package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class LongExpr implements Serializable {

  public abstract <T, U> T accept(LongExprVisitor<T, U> visitor, U arg);

  @Override
  public abstract boolean equals(Object obj);

  public abstract long evaluate(Environment environment);

  @Override
  public abstract int hashCode();
}
