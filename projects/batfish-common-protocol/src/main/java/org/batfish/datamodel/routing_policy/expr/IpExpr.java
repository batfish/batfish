package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/** Base class for expressions that return an {@link Ip} address. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class IpExpr implements Serializable {

  @Override
  public abstract boolean equals(Object obj);

  public abstract Ip evaluate(Environment env);

  @Override
  public abstract int hashCode();
}
