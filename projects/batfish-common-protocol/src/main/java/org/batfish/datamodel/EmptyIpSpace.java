package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class EmptyIpSpace extends IpSpace {

  /** */
  private static final long serialVersionUID = 1L;

  public static final IpSpace INSTANCE = new EmptyIpSpace();

  private EmptyIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitEmptyIpSpace(this);
  }

  @Override
  public boolean containsIp(Ip ip) {
    return false;
  }

  @Override
  public IpSpace complement() {
    return UniverseIpSpace.INSTANCE;
  }

  @Override
  public String toString() {
    return "empty";
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return 0;
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }
}
