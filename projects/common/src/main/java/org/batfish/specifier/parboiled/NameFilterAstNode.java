package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NameFilterAstNode implements FilterAstNode {
  private final String _name;

  NameFilterAstNode(AstNode nameAst) {
    this(((StringAstNode) nameAst).getStr());
  }

  NameFilterAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameFilterAstNode(this);
  }

  @Override
  public <T> T accept(FilterAstNodeVisitor<T> visitor) {
    return visitor.visitNameFilterAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameFilterAstNode)) {
      return false;
    }
    NameFilterAstNode that = (NameFilterAstNode) o;
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
