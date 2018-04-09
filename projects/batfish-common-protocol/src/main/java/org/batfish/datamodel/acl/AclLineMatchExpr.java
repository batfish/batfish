package org.batfish.datamodel.acl;

import java.io.Serializable;

public abstract class AclLineMatchExpr implements Serializable {
  private static final long serialVersionUID = 1L;

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

  public abstract <R> R accept(GenericAclLineMatchExprVisitor<R> visitor);
}
