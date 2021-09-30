package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} representing a boolean constraint that Batfish is incapable of evaluating. May
 * be evaluated as {@code true} or {@code false} depending on context.
 */
@ParametersAreNonnullByDefault
public final class UnhandledAstNode extends VariableInspectTextBooleanExprAstNode {

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitUnhandledAstNode(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return baseEquals(obj);
  }

  @Override
  public int hashCode() {
    return baseHashCode();
  }

  @Override
  public String toString() {
    return baseToStringHelper().toString();
  }

  public static @Nonnull UnhandledAstNode of(String unhandledText) {
    return new UnhandledAstNode(unhandledText);
  }

  private UnhandledAstNode(String inspectText) {
    super(inspectText);
  }
}
