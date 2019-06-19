package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class FalseExpr extends AclLineMatchExpr {
  private static final long serialVersionUID = 1L;
  public static final FalseExpr INSTANCE = new FalseExpr();

  private FalseExpr() {
    super(null);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitFalseExpr(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(((Boolean) false));
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return 0;
  }
}
