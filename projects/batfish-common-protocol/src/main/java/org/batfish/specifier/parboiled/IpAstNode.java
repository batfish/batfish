package org.batfish.specifier.parboiled;

import java.util.Objects;
import org.batfish.datamodel.Ip;

final class IpAstNode implements IpSpaceAstNode {
  private final Ip _ip;

  IpAstNode(Ip ip) {
    _ip = ip;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIpAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitIpAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpAstNode)) {
      return false;
    }
    IpAstNode ipAstNode = (IpAstNode) o;
    return Objects.equals(_ip, ipAstNode._ip);
  }

  public Ip getIp() {
    return _ip;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip);
  }
}
