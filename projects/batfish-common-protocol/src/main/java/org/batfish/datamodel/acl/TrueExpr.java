package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class TrueExpr extends AclLineMatchExpr {
  public static final TrueExpr INSTANCE = new TrueExpr();

  private TrueExpr() {
    super(null);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitTrueExpr(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return 0;
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode((Boolean) true);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }
}
