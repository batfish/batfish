package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.regex.Pattern;

final class NameRegexRoutingPolicyAstNode implements RoutingPolicyAstNode {
  private final String _regex;
  private final Pattern _pattern;

  NameRegexRoutingPolicyAstNode(AstNode regexAst) {
    this(((StringAstNode) regexAst).getStr());
  }

  NameRegexRoutingPolicyAstNode(String regex) {
    _regex = regex;
    _pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameRegexRoutingPolicyAstNode(this);
  }

  @Override
  public <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor) {
    return visitor.visitNameRegexRoutingPolicyAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameRegexRoutingPolicyAstNode)) {
      return false;
    }
    NameRegexRoutingPolicyAstNode that = (NameRegexRoutingPolicyAstNode) o;
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
