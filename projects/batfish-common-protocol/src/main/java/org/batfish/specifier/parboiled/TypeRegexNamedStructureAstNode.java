package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.regex.Pattern;

final class TypeRegexNamedStructureAstNode implements NamedStructureAstNode {
  private final String _regex;
  private final Pattern _pattern;

  TypeRegexNamedStructureAstNode(AstNode regexAst) {
    _regex = ((RegexAstNode) regexAst).getRegex();
    _pattern = ((RegexAstNode) regexAst).getPattern();
  }

  TypeRegexNamedStructureAstNode(String regex) {
    _regex = regex;
    _pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTypeRegexNamedStructureAstNode(this);
  }

  @Override
  public <T> T accept(NamedStructureAstNodeVisitor<T> visitor) {
    return visitor.visitTypeRegexNamedStructureAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeRegexNamedStructureAstNode)) {
      return false;
    }
    TypeRegexNamedStructureAstNode that = (TypeRegexNamedStructureAstNode) o;
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
