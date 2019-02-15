package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class VrfInterfaceAstNode implements InterfaceAstNode {
  private final StringAstNode _vrfNameAstNode;

  VrfInterfaceAstNode(AstNode vrfName) {
    _vrfNameAstNode = (StringAstNode) vrfName;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitVrfInterfaceSpecAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitVrfInterfaceSpecAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VrfInterfaceAstNode that = (VrfInterfaceAstNode) o;
    return Objects.equals(_vrfNameAstNode, that._vrfNameAstNode);
  }

  public StringAstNode getVrfNameAstNode() {
    return _vrfNameAstNode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vrfNameAstNode);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass())
        .add("vrfNameAstNode", _vrfNameAstNode)
        .toString();
  }
}
