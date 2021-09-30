package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} representing the condition that the packet direction is classified as {@code
 * incoming}.
 */
@ParametersAreNonnullByDefault
public final class IncomingAstNode extends VariableInspectTextBooleanExprAstNode {

  public IncomingAstNode(String inspectText) {
    super(inspectText);
  }

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitIncomingAstNode(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return baseEquals(obj);
  }

  @Override
  public int hashCode() {
    return baseHashCode();
  }
}
