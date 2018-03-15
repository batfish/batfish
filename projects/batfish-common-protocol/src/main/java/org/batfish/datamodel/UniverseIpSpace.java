package org.batfish.datamodel;

import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class UniverseIpSpace implements IpSpace {

  public static final UniverseIpSpace INSTANCE = new UniverseIpSpace();

  private UniverseIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitUniverseIpSpace(this);
  }

  @Override
  public boolean contains(@Nonnull Ip ip) {
    return true;
  }
}
