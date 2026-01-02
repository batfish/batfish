package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;

/**
 * Match condition that holds when flow originated from this device rather than being received on
 * some interface.
 */
public class OriginatingFromDevice extends AclLineMatchExpr {

  public static final OriginatingFromDevice INSTANCE = new OriginatingFromDevice();

  private OriginatingFromDevice() {
    super(null);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitOriginatingFromDevice(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }
}
