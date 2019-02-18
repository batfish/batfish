package org.batfish.specifier.parboiled;

final class DifferenceFilterAstNode extends SetOpFilterAstNode {

  DifferenceFilterAstNode(FilterAstNode left, FilterAstNode right) {
    _left = left;
    _right = right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceFilterAstNode(this);
  }

  @Override
  public <T> T accept(FilterAstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceFilterAstNode(this);
  }
}
