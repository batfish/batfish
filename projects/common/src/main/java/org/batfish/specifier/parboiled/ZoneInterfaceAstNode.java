package org.batfish.specifier.parboiled;

import java.util.Objects;

final class ZoneInterfaceAstNode implements InterfaceAstNode {
  private final String _zoneName;

  ZoneInterfaceAstNode(AstNode zoneNameAstNode) {
    this(((StringAstNode) zoneNameAstNode).getStr());
  }

  ZoneInterfaceAstNode(String zoneName) {
    _zoneName = zoneName;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitZoneInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitZoneInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZoneInterfaceAstNode)) {
      return false;
    }
    ZoneInterfaceAstNode that = (ZoneInterfaceAstNode) o;
    return Objects.equals(_zoneName, that._zoneName);
  }

  public String getZoneName() {
    return _zoneName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_zoneName);
  }
}
