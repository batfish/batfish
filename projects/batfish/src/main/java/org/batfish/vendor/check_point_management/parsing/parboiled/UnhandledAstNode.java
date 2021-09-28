package org.batfish.vendor.check_point_management.parsing.parboiled;

import static com.google.common.base.MoreObjects.toStringHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} representing a boolean constraint that Batfish is incapable of evaluating. May
 * be evaluated as {@code true} or {@code false} depending on context.
 */
@ParametersAreNonnullByDefault
public final class UnhandledAstNode extends BooleanExprAstNode {

  public @Nonnull String getUnhandledText() {
    return _unhandledText;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof UnhandledAstNode)) {
      return false;
    }
    UnhandledAstNode that = (UnhandledAstNode) obj;
    return _unhandledText.equals(that._unhandledText);
  }

  @Override
  public int hashCode() {
    return _unhandledText.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_unhandledText", _unhandledText).toString();
  }

  public static @Nonnull UnhandledAstNode of(String unhandledText) {
    return new UnhandledAstNode(unhandledText);
  }

  private UnhandledAstNode(String unhandledText) {
    _unhandledText = unhandledText;
  }

  private final @Nonnull String _unhandledText;
}
