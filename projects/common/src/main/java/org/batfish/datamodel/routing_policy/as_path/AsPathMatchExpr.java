package org.batfish.datamodel.routing_policy.as_path;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;

/** An epression that matches an {@link org.batfish.datamodel.AsPath}. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AsPathMatchExpr implements Serializable {

  public abstract <T, U> T accept(AsPathMatchExprVisitor<T, U> visitor, U arg);

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public abstract int hashCode();
}
