package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class VrfInterfaceAstNode implements InterfaceAstNode {
  private final String _vrfName;

  VrfInterfaceAstNode(AstNode vrfNameAstNode) {
    this(((StringAstNode) vrfNameAstNode).getStr());
  }

  VrfInterfaceAstNode(String vrfName) {
    _vrfName = vrfName;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitVrfInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitVrfInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VrfInterfaceAstNode)) {
      return false;
    }
    VrfInterfaceAstNode that = (VrfInterfaceAstNode) o;
    return Objects.equals(_vrfName, that._vrfName);
  }

  public String getVrfName() {
    return _vrfName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_vrfName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("vrfName", _vrfName).toString();
  }
}
