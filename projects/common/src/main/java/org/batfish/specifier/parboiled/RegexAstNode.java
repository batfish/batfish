package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class RegexAstNode implements AstNode {
  private final String _regex;
  private final Pattern _pattern;

  RegexAstNode(String regex) {
    _regex = regex;
    try {
      _pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException e) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid regex /%s/: %s near index %d", _regex, e.getDescription(), e.getIndex()));
    }
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitRegexAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegexAstNode)) {
      return false;
    }
    RegexAstNode that = (RegexAstNode) o;
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
