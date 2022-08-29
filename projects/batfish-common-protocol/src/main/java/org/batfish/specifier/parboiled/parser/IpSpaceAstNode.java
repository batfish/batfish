package org.batfish.specifier.parboiled.parser;

import javax.annotation.Nonnull;

interface IpSpaceAstNode extends AstNode {
  @Nonnull
  <T> T accept(IpSpaceAstNodeVisitor<T> visitor);
}
