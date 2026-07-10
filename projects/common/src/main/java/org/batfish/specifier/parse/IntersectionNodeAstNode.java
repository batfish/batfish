package org.batfish.specifier.parse;

final class IntersectionNodeAstNode extends SetOpNodeAstNode {

  IntersectionNodeAstNode(AstNode left, AstNode right) {
    super((NodeAstNode) left, (NodeAstNode) right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionNodeAstNode(this);
  }
}
