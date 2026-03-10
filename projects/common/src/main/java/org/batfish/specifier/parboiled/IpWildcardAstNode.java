package org.batfish.specifier.parboiled;

import java.util.Objects;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;

final class IpWildcardAstNode implements IpSpaceAstNode {

  private final IpWildcard _ipWildcard;

  IpWildcardAstNode(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  IpWildcardAstNode(String ipWildcard) {
    _ipWildcard = IpWildcard.parse(ipWildcard);
  }

  IpWildcardAstNode(AstNode addressNode, AstNode maskNode) {
    Ip address = ((IpAstNode) addressNode).getIp();
    Ip mask = ((IpAstNode) maskNode).getIp();
    _ipWildcard = IpWildcard.ipWithWildcardMask(address, mask);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIpWildcardAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitIpWildcardAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpWildcardAstNode)) {
      return false;
    }
    IpWildcardAstNode that = (IpWildcardAstNode) o;
    return Objects.equals(_ipWildcard, that._ipWildcard);
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ipWildcard);
  }
}
