package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AclLineMatchExpr implements Serializable, Comparable<AclLineMatchExpr> {
  private static final long serialVersionUID = 1L;

  public abstract <R> R accept(GenericAclLineMatchExprVisitor<R> visitor);

  @Override
  public final int compareTo(AclLineMatchExpr o) {
    if (this == o) {
      return 0;
    }
    int ret;
    ret = getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
    if (ret != 0) {
      return ret;
    }
    return compareSameClass(o);
  }

  protected abstract int compareSameClass(AclLineMatchExpr o);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(getClass() == o.getClass())) {
      return false;
    }
    return exprEquals(o);
  }

  protected abstract boolean exprEquals(Object o);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
