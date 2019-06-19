package org.batfish.datamodel;

import java.util.Map;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class EmptyIpSpace extends IpSpace {

  public static final IpSpace INSTANCE = new EmptyIpSpace();

  private static final long serialVersionUID = 1L;

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
  public boolean containsIp(Ip ip, Map<String, IpSpace> namedIpSpaces) {
    return false;
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
