package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Objects;
import org.batfish.datamodel.IpProtocol;

final class IpProtocolIpProtocolAstNode implements IpProtocolAstNode {
  private final IpProtocol _ipProtocol;

  static boolean isValidName(String name) {
    return Arrays.stream(IpProtocol.values()).anyMatch(p -> p.toString().equalsIgnoreCase(name));
  }

  static boolean isValidNumber(int number) {
    return number >= 0 && number < 256;
  }

  IpProtocolIpProtocolAstNode(String nameOrNum) {
    _ipProtocol = IpProtocol.fromString(nameOrNum);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIpProtocolIpProtocolAstNode(this);
  }

  @Override
  public <T> T accept(IpProtocolAstNodeVisitor<T> visitor) {
    return visitor.visitIpProtocolIpProtocolAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpProtocolIpProtocolAstNode)) {
      return false;
    }
    IpProtocolIpProtocolAstNode that = (IpProtocolIpProtocolAstNode) o;
    return Objects.equals(_ipProtocol, that._ipProtocol);
  }

  public IpProtocol getIpProtocol() {
    return _ipProtocol;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ipProtocol.ordinal());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("ipProtocol", _ipProtocol).toString();
  }
}
