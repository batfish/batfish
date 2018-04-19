package org.batfish.datamodel;

import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class UniverseIpSpace extends IpSpace {

  /** */
  private static final long serialVersionUID = 1L;

  public static final UniverseIpSpace INSTANCE = new UniverseIpSpace();

  private UniverseIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitUniverseIpSpace(this);
  }

  @Override
  public IpSpace complement() {
    return EmptyIpSpace.INSTANCE;
  }

  @Override
  public boolean containsIp(@Nonnull Ip ip) {
    return true;
  }

  @Override
  public String toString() {
    return "universe";
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
