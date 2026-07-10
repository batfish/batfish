package org.batfish.specifier.parse;

import javax.annotation.Nonnull;

interface IpSpaceAstNode extends AstNode {
  @Nonnull
  <T> T accept(IpSpaceAstNodeVisitor<T> visitor);
}
