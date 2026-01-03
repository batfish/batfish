package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NameRoutingPolicyAstNode implements RoutingPolicyAstNode {
  private final String _name;

  NameRoutingPolicyAstNode(AstNode nameAst) {
    this(((StringAstNode) nameAst).getStr());
  }

  NameRoutingPolicyAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameRoutingPolicyAstNode(this);
  }

  @Override
  public <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor) {
    return visitor.visitNameRoutingPolicyAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRoutingPolicyAstNode)) {
      return false;
    }
    NameRoutingPolicyAstNode that = (NameRoutingPolicyAstNode) o;
    return Objects.equals(_name, that._name);
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("name", _name).toString();
  }
}
