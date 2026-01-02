package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class NextHopExpr implements Serializable {

  public abstract @Nullable NextHop evaluate(Environment env);

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();
}
