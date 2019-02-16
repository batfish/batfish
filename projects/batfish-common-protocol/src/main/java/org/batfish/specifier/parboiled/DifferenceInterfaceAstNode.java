package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

class DifferenceInterfaceAstNode extends SetOpInterfaceAstNode {

  DifferenceInterfaceAstNode(InterfaceAstNode left, InterfaceAstNode right) {
    _left = left;
    _right = right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DifferenceInterfaceAstNode)) {
      return false;
    }
    DifferenceInterfaceAstNode that = (DifferenceInterfaceAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass())
        .add("left", _left)
        .add("right", "_right")
        .toString();
  }
}
