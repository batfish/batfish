package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class EmptyIpSpace implements JacksonSerializableIpSpace {

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
  public IpSpace unwrap() {
    return this;
  }
}
