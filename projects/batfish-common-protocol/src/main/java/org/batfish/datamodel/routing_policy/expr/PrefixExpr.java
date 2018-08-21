package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

/** Base class for expressions that extract a {@link Prefix}. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class PrefixExpr implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  @Override
  public abstract boolean equals(Object obj);

  public abstract Prefix evaluate(Environment env);

  @Override
  public abstract int hashCode();
}
