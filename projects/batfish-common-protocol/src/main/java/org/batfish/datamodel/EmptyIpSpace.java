package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class EmptyIpSpace extends IpSpace {

  public static final EmptyIpSpace INSTANCE = new EmptyIpSpace();

  private EmptyIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitEmptyIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return 0;
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }

  @Override
  public String toString() {
    return "empty";
  }
}
