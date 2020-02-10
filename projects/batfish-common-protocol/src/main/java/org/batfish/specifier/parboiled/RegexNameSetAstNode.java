package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.regex.Pattern;

/** Represents the AST node for a regex over set of the names */
final class RegexNameSetAstNode implements NameSetAstNode {
  private final String _regex;
  private final Pattern _pattern;

  RegexNameSetAstNode(AstNode regexAst) {
    _regex = ((RegexAstNode) regexAst).getRegex();
    _pattern = ((RegexAstNode) regexAst).getPattern();
  }

  RegexNameSetAstNode(String regex) {
    _regex = regex;
    _pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitRegexNameSetAstNode(this);
  }

  @Override
  public <T> T accept(NameSetAstNodeVisitor<T> visitor) {
    return visitor.visitRegexNameSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegexNameSetAstNode)) {
      return false;
    }
    RegexNameSetAstNode that = (RegexNameSetAstNode) o;
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
    return Objects.hashCode(_regex);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("regex", _regex).toString();
  }
}
