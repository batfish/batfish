package org.batfish.specifier.parboiled;

import org.parboiled.trees.ImmutableBinaryTreeNode;

/** The base class of all AST nodes. */
public abstract class AstNode extends ImmutableBinaryTreeNode<AstNode> {

  public AstNode(AstNode left, AstNode right) {
    super(left, right);
  }

  public abstract Object getValue();

  @Override
  public String toString() {
    return getValue().toString();
  }
}
