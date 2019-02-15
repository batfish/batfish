package org.batfish.specifier.parboiled;

abstract class SetOpInterfaceAstNode implements InterfaceAstNode {
  protected InterfaceAstNode _left;
  protected InterfaceAstNode _right;

  static SetOpInterfaceAstNode create(Character c, AstNode left, AstNode right) {
    InterfaceAstNode leftSpec = (InterfaceAstNode) left;
    InterfaceAstNode rightSpec = (InterfaceAstNode) right;
    switch (c) {
      case '+':
        return new UnionInterfaceAstNode(leftSpec, rightSpec);
      case '-':
        return new DifferenceInterfaceAstNode(leftSpec, rightSpec);
      default:
        throw new IllegalStateException("Unknown set operation for interface spec " + c);
    }
  }

  public InterfaceAstNode getLeft() {
    return _left;
  }

  public InterfaceAstNode getRight() {
    return _right;
  }
}
