package org.batfish.datamodel;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class ComplementIpSpace implements IpSpace {

  private final IpSpace _ipSpace;

  public ComplementIpSpace(@Nonnull IpSpace ipSpace) {
    _ipSpace = ipSpace;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitComplementIpSpace(this);
  }

  @Override
  public boolean containsIp(Ip ip) {
    return !_ipSpace.containsIp(ip);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof ComplementIpSpace)) {
      return false;
    }
    return Objects.equals(_ipSpace, ((ComplementIpSpace) o)._ipSpace);
  }

  public IpSpace getIpSpace() {
    return _ipSpace;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ipSpace);
  }

  @Override
  public String toString() {
    return String.format("complementOf(%s)", _ipSpace);
  }
}
