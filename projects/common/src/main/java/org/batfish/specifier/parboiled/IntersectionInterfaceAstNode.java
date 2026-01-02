package org.batfish.specifier.parboiled;

final class IntersectionInterfaceAstNode extends SetOpInterfaceAstNode {

  IntersectionInterfaceAstNode(AstNode left, AstNode right) {
    super((InterfaceAstNode) left, (InterfaceAstNode) right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionInterfaceAstNode(this);
  }
}
