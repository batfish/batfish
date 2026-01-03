package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

final class RoleNodeAstNode implements NodeAstNode {
  private final String _roleName;
  private final String _dimensionName;

  RoleNodeAstNode(AstNode dimensionName, AstNode roleName) {
    checkArgument(roleName instanceof StringAstNode, "role name must be a string");
    checkArgument(dimensionName instanceof StringAstNode, "dimension name must be a string");
    _dimensionName = ((StringAstNode) dimensionName).getStr();
    _roleName = ((StringAstNode) roleName).getStr();
  }

  RoleNodeAstNode(String dimensionName, String roleName) {
    _dimensionName = dimensionName;
    _roleName = roleName;
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
    return Objects.equals(_dimensionName, that._dimensionName)
        && Objects.equals(_roleName, that._roleName);
  }

  String getDimensionName() {
    return _dimensionName;
  }

  String getRoleName() {
    return _roleName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_dimensionName, _roleName);
  }
}
