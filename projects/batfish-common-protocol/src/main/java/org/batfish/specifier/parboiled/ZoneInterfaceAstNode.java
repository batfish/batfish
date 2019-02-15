package org.batfish.specifier.parboiled;

import java.util.Objects;

final class ZoneInterfaceAstNode implements InterfaceAstNode {
  private final StringAstNode _zoneNameAstNode;

  ZoneInterfaceAstNode(AstNode zoneName) {
    _zoneNameAstNode = (StringAstNode) zoneName;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitZoneInterfaceSpecAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitZoneInterfaceSpecAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZoneInterfaceAstNode that = (ZoneInterfaceAstNode) o;
    return Objects.equals(_zoneNameAstNode, that._zoneNameAstNode);
  }

  public StringAstNode getZoneNameAstNode() {
    return _zoneNameAstNode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_zoneNameAstNode);
  }
}
