package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

final class RoleNodeAstNode implements NodeAstNode {
  private final String _roleName;
  private final String _dimensionName;

  RoleNodeAstNode(AstNode roleName, AstNode dimensionName) {
    checkArgument(roleName instanceof StringAstNode, "role name must be a string");
    checkArgument(dimensionName instanceof StringAstNode, "dimension name must be a string");
    _roleName = ((StringAstNode) roleName).getStr();
    _dimensionName = ((StringAstNode) dimensionName).getStr();
  }

  RoleNodeAstNode(String roleName, String dimensionName) {
    _roleName = roleName;
    _dimensionName = dimensionName;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitRoleNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitRoleNodeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoleNodeAstNode)) {
      return false;
    }
    RoleNodeAstNode that = (RoleNodeAstNode) o;
    return Objects.equals(_roleName, that._roleName)
        && Objects.equals(_dimensionName, that._dimensionName);
  }

  String getRoleName() {
    return _roleName;
  }

  String getDimensionName() {
    return _dimensionName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_roleName, _dimensionName);
  }
}
