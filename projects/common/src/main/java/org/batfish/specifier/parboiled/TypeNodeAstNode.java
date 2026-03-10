package org.batfish.specifier.parboiled;

import java.util.Objects;
import org.batfish.datamodel.DeviceType;

final class TypeNodeAstNode implements NodeAstNode {
  private final DeviceType _deviceType;

  TypeNodeAstNode(AstNode typeAstNode) {
    this(((StringAstNode) typeAstNode).getStr());
  }

  TypeNodeAstNode(String typeStr) {
    _deviceType = Enum.valueOf(DeviceType.class, typeStr.toUpperCase());
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTypeNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitTypeNodeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeNodeAstNode)) {
      return false;
    }
    TypeNodeAstNode that = (TypeNodeAstNode) o;
    return Objects.equals(_deviceType, that._deviceType);
  }

  public DeviceType getDeviceType() {
    return _deviceType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_deviceType);
  }
}
