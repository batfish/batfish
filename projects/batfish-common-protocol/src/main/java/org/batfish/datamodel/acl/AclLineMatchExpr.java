package org.batfish.datamodel.acl;

public abstract class AclLineMatchExpr {
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

  public abstract int hashCode();

  public abstract String toString();

  public abstract <R> R accept(GenericAclLineMatchExprVisitor<R> visitor);
}
