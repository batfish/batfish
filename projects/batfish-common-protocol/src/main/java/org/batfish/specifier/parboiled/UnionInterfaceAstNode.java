package org.batfish.specifier.parboiled;

final class UnionInterfaceAstNode extends SetOpInterfaceAstNode {

  UnionInterfaceAstNode(InterfaceAstNode left, InterfaceAstNode right) {
    _left = left;
    _right = right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitUnionInterfaceAstNode(this);
  }
}
