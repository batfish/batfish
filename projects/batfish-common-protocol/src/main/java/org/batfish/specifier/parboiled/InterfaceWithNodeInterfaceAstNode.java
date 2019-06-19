package org.batfish.specifier.parboiled;

import java.util.Objects;

final class InterfaceWithNodeInterfaceAstNode implements InterfaceAstNode {
  private final NodeAstNode _nodeAstNode;
  private final InterfaceAstNode _interfaceAstNode;

  InterfaceWithNodeInterfaceAstNode(AstNode nodeAstNode, AstNode interfaceAstNode) {
    _nodeAstNode = (NodeAstNode) nodeAstNode;
    _interfaceAstNode = (InterfaceAstNode) interfaceAstNode;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitInterfaceWithNodeInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitInterfaceWithNodeInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceWithNodeInterfaceAstNode)) {
      return false;
    }
    InterfaceWithNodeInterfaceAstNode that = (InterfaceWithNodeInterfaceAstNode) o;
    return Objects.equals(_nodeAstNode, that._nodeAstNode)
        && Objects.equals(_interfaceAstNode, that._interfaceAstNode);
  }

  public NodeAstNode getNodeAstNode() {
    return _nodeAstNode;
  }

  public InterfaceAstNode getInterfaceAstNode() {
    return _interfaceAstNode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeAstNode, _interfaceAstNode);
  }
}
