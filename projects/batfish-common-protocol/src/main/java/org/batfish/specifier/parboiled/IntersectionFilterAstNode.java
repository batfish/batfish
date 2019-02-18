package org.batfish.specifier.parboiled;

final class IntersectionFilterAstNode extends SetOpFilterAstNode {

  IntersectionFilterAstNode(AstNode left, AstNode right) {
    _left = (FilterAstNode) left;
    _right = (FilterAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionFilterAstNode(this);
  }

  @Override
  public <T> T accept(FilterAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionFilterAstNode(this);
  }
}
