package org.batfish.specifier.parboiled;

final class StringAstNode implements AstNode {
  private final String _str;

  StringAstNode(String str) {
    _str = str;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitStringAstNode(this);
  }

  public String getStr() {
    return _str;
  }
}
