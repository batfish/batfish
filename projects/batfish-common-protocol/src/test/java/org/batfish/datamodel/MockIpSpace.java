package org.batfish.datamodel;

import java.util.Objects;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class MockIpSpace implements IpSpace {

  private final int _num;

  public MockIpSpace(int num) {
    _num = num;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public IpSpace complement() {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean containsIp(Ip ip) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof MockIpSpace)) {
      return false;
    }
    return _num == ((MockIpSpace) o)._num;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_num);
  }

  @Override
  public String toString() {
    return String.format("TestIpSpace%d", _num);
  }
}
