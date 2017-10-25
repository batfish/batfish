package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.Serializable;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class BooleanExpr implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_COMMENT = "comment";

  private String _comment;

  @Override
  public abstract boolean equals(Object obj);

  public abstract Result evaluate(Environment environment);

  @JsonProperty(PROP_COMMENT)
  public String getComment() {
    return _comment;
  }

  @Override
  public abstract int hashCode();

  @JsonProperty(PROP_COMMENT)
  public void setComment(String comment) {
    _comment = comment;
  }

  public BooleanExpr simplify() {
    return this;
  }

  /**
   * Used by extenders to build a {@link Object#toString} function that includes parent properties.
   */
  ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(getClass()).omitNullValues().add("comment", _comment);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }
}
