package org.batfish.specifier.parboiled;

import org.parboiled.trees.ImmutableBinaryTreeNode;

/** The base class of all AST nodes. */
abstract class AstNode extends ImmutableBinaryTreeNode<AstNode> {

  AstNode(AstNode left, AstNode right) {
    super(left, right);
  }
}
