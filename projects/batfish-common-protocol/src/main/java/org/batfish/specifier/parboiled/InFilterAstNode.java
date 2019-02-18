package org.batfish.specifier.parboiled;

final class InFilterAstNode extends DirectionFilterAstNode {

  public InFilterAstNode(InterfaceAstNode interfaceAst) {
    _interfaceAst = interfaceAst;
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
