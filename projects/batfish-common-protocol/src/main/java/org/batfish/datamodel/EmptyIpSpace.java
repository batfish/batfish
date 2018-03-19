package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class EmptyIpSpace implements IpSpace {

  public static final IpSpace INSTANCE = new EmptyIpSpace();

  private EmptyIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitEmptyIpSpace(this);
  }

  @Override
  public boolean contains(Ip ip) {
    return false;
  }
}
