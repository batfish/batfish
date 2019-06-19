package org.batfish.datamodel;

import java.util.Map;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class UniverseIpSpace extends IpSpace {

  public static final UniverseIpSpace INSTANCE = new UniverseIpSpace();

  private static final long serialVersionUID = 1L;

  private UniverseIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitUniverseIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return 0;
  }

  @Override
  public boolean containsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    return true;
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
    return "universe";
  }
}
