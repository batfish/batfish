package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NameAppAstNode implements AppAstNode {
  private final String _name;

  NameAppAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitNameAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameAppAstNode)) {
      return false;
    }
    NameAppAstNode that = (NameAppAstNode) o;
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
