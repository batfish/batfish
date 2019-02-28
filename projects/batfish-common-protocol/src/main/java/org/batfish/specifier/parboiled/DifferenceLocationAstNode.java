package org.batfish.specifier.parboiled;

final class DifferenceLocationAstNode extends SetOpLocationAstNode {

  DifferenceLocationAstNode(LocationAstNode left, LocationAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceLocationAstNode(this);
  }

  @Override
  public <T> T accept(LocationAstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceLocationAstNode(this);
  }
}
