package org.batfish.specifier.parboiled;

final class UnionNodeAstNode extends SetOpNodeAstNode {

  UnionNodeAstNode(NodeAstNode left, NodeAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitUnionNodeAstNode(this);
  }
}
