package org.batfish.specifier.parboiled;

import javax.annotation.Nonnull;

interface IpSpaceAstNode extends AstNode {
  @Nonnull
  <T> T accept(IpSpaceAstNodeVisitor<T> visitor);
}
