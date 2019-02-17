package org.batfish.specifier.parboiled;

abstract class SetOpNodeAstNode implements NodeAstNode {
  protected NodeAstNode _left;
  protected NodeAstNode _right;

  static SetOpNodeAstNode create(Character c, AstNode left, AstNode right) {
    NodeAstNode leftSpec = (NodeAstNode) left;
    NodeAstNode rightSpec = (NodeAstNode) right;
    switch (c) {
      case '+':
        return new UnionNodeAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceNodeAstNode(leftSpec, rightSpec);
      default:
        throw new IllegalStateException("Unknown set operation for node spec " + c);
    }
  }

  public NodeAstNode getLeft() {
    return _left;
  }

  public NodeAstNode getRight() {
    return _right;
  }
}
