package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NameNodeAstNode implements NodeAstNode {
  private final String _name;

  NameNodeAstNode(AstNode nameAst) {
    this(((StringAstNode) nameAst).getStr());
  }

  NameNodeAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitNameNodeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameNodeAstNode)) {
      return false;
    }
    NameNodeAstNode that = (NameNodeAstNode) o;
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
