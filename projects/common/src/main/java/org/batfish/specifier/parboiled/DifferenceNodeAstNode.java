package org.batfish.specifier.parboiled;

final class DifferenceNodeAstNode extends SetOpNodeAstNode {

  DifferenceNodeAstNode(NodeAstNode left, NodeAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceNodeAstNode(this);
  }
}
