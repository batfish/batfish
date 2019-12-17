package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.IpProtocol;

final class NotIpProtocolAstNode implements IpProtocolAstNode {
  private final IpProtocol _ipProtocol;

  NotIpProtocolAstNode(String nameOrNum) {
    this(new IpProtocolIpProtocolAstNode(nameOrNum));
  }

  NotIpProtocolAstNode(AstNode astNode) {
    IpProtocolIpProtocolAstNode ipAst = (IpProtocolIpProtocolAstNode) astNode;
    _ipProtocol = ipAst.getIpProtocol();
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNotIpProtocolAstNode(this);
  }

  @Override
  public <T> T accept(IpProtocolAstNodeVisitor<T> visitor) {
    return visitor.visitNotIpProtocolAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NotIpProtocolAstNode)) {
      return false;
    }
    NotIpProtocolAstNode that = (NotIpProtocolAstNode) o;
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
