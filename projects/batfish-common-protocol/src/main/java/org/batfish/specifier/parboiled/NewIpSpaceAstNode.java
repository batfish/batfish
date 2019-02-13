package org.batfish.specifier.parboiled;

interface NewIpSpaceAstNode extends AstNode {
  <T> T accept(IpSpaceAstNodeVisitor<T> visitor);
}
