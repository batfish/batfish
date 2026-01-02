package org.batfish.specifier.parboiled;

import java.util.Objects;

final class ConnectedToInterfaceAstNode implements InterfaceAstNode {
  private final IpSpaceAstNode _ipSpaceAstNode;

  ConnectedToInterfaceAstNode(AstNode ipSpace) {
    _ipSpaceAstNode = (IpSpaceAstNode) ipSpace;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitConnectedToInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitConnectedToInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConnectedToInterfaceAstNode)) {
      return false;
    }
    ConnectedToInterfaceAstNode that = (ConnectedToInterfaceAstNode) o;
    return Objects.equals(_ipSpaceAstNode, that._ipSpaceAstNode);
  }

  public IpSpaceAstNode getIpSpaceAstNode() {
    return _ipSpaceAstNode;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ipSpaceAstNode);
  }
}
