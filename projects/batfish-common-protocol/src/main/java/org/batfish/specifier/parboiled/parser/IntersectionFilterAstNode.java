package org.batfish.specifier.parboiled.parser;

final class IntersectionFilterAstNode extends SetOpFilterAstNode {

  IntersectionFilterAstNode(AstNode left, AstNode right) {
    super((FilterAstNode) left, (FilterAstNode) right);
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
