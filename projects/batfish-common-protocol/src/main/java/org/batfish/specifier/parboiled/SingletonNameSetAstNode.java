package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/** Represents the AST node for one specific name from the name set */
final class SingletonNameSetAstNode implements NameSetAstNode {
  private final String _name;

  SingletonNameSetAstNode(AstNode nameAst) {
    this(((StringAstNode) nameAst).getStr());
  }

  SingletonNameSetAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameNameSetAstNode(this);
  }

  @Override
  public <T> T accept(NameSetAstNodeVisitor<T> visitor) {
    return visitor.visitNameNameSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SingletonNameSetAstNode)) {
      return false;
    }
    return Objects.equals(_name, ((SingletonNameSetAstNode) o)._name);
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
