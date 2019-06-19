package org.batfish.specifier.parboiled;

final class IntersectionLocationAstNode extends SetOpLocationAstNode {

  IntersectionLocationAstNode(AstNode left, AstNode right) {
    super((LocationAstNode) left, (LocationAstNode) right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionLocationAstNode(this);
  }

  @Override
  public <T> T accept(LocationAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionLocationAstNode(this);
  }
}
