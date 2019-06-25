package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AsExpr implements Serializable {

  @Override
  public abstract boolean equals(Object obj);

  public abstract long evaluate(Environment environment);

  @Override
  public abstract int hashCode();
}
