package org.batfish.specifier.parboiled;

final class UnionLocationAstNode extends SetOpLocationAstNode {

  UnionLocationAstNode(LocationAstNode left, LocationAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionLocationAstNode(this);
  }

  @Override
  public <T> T accept(LocationAstNodeVisitor<T> visitor) {
    return visitor.visitUnionLocationAstNode(this);
  }
}
