package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing a comparator. */
@ParametersAreNonnullByDefault
public interface ComparatorAstNode extends AstNode {

  <T, U> T accept(ComparatorAstNodeVisitor<T, U> visitor, U value);
}
