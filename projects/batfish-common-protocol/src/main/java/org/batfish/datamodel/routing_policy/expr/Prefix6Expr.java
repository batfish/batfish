package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Prefix6Expr implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  @Override
  public abstract boolean equals(Object obj);

  public abstract Prefix6 evaluate(Environment env);

  @Override
  public abstract int hashCode();
}
