package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NotEnumSetAstNode implements EnumSetAstNode {
  private final EnumSetAstNode _astNode;

  NotEnumSetAstNode(AstNode astNode) {
    _astNode = (EnumSetAstNode) astNode;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNotEnumSetAstNode(this);
  }

  @Override
  public <T> T accept(EnumSetAstNodeVisitor<T> visitor) {
    return visitor.visitNotEnumSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NotEnumSetAstNode)) {
      return false;
    }
    NotEnumSetAstNode that = (NotEnumSetAstNode) o;
    return Objects.equals(_astNode, that._astNode);
  }

  public EnumSetAstNode getAstNode() {
    return _astNode;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_astNode);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("astNode", _astNode).toString();
  }
}
