package org.batfish.vendor.check_point_management.parsing.parboiled;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.base.MoreObjects.ToStringHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A {@link BooleanExprAstNode} creatable from any of a class of equivalent INSPECT expressions. */
public abstract class VariableInspectTextBooleanExprAstNode
    implements BooleanExprAstNode, HasInspectText {

  protected VariableInspectTextBooleanExprAstNode(String inspectText) {
    _inspectText = inspectText;
  }

  @Override
  public final @Nonnull String getInspectText() {
    return _inspectText;
  }

  protected boolean baseEquals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!getClass().isInstance(o)) {
      return false;
    }
    VariableInspectTextBooleanExprAstNode that = (VariableInspectTextBooleanExprAstNode) o;
    return _inspectText.equals(that._inspectText);
  }

  protected int baseHashCode() {
    return _inspectText.hashCode();
  }

  protected @Nonnull ToStringHelper baseToStringHelper() {
    return toStringHelper(this).add("_inspectText", _inspectText);
  }

  private final @Nonnull String _inspectText;
}
