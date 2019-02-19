package org.batfish.specifier.parboiled;

final class InFilterAstNode extends DirectionFilterAstNode {

  InFilterAstNode(InterfaceAstNode interfaceAst) {
    super(interfaceAst);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitInFilterAstNode(this);
  }

  @Override
  public <T> T accept(FilterAstNodeVisitor<T> visitor) {
    return visitor.visitInFilterAstNode(this);
  }
}
