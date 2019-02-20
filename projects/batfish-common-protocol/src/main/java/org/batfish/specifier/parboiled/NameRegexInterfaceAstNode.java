package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.regex.Pattern;

final class NameRegexInterfaceAstNode implements InterfaceAstNode {
  private final String _regex;
  private final Pattern _pattern;

  NameRegexInterfaceAstNode(String regex) {
    _regex = regex;
    // treat the provided pattern as a case-insensitive substring match
    _pattern = Pattern.compile(String.format(".*%s.*", regex), Pattern.CASE_INSENSITIVE);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameRegexInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitNameRegexInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRegexInterfaceAstNode)) {
      return false;
    }
    NameRegexInterfaceAstNode that = (NameRegexInterfaceAstNode) o;
    return Objects.equals(_regex, that._regex);
  }

  public String getRegex() {
    return _regex;
  }

  public Pattern getPattern() {
    return _pattern;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_regex);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass()).add("regex", _regex).toString();
  }
}
