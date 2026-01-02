package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.Serializable;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AsPathSetExpr implements Serializable {

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  public abstract boolean matches(Environment environment);

  /**
   * Used by extenders to build a {@link Object#toString} function that includes parent properties.
   */
  ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(getClass()).omitNullValues();
  }
}
