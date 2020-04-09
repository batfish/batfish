package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class StringAstNode implements AstNode {
  private final String _str;

  StringAstNode(String str) {
    _str = str;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitStringAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StringAstNode)) {
      return false;
    }
    StringAstNode that = (StringAstNode) o;
    return Objects.equals(_str, that._str);
  }

  public String getStr() {
    return _str;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_str);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("str", _str).toString();
  }
}
