package org.batfish.specifier.parboiled.parser;

final class OutFilterAstNode extends DirectionFilterAstNode {

  OutFilterAstNode(AstNode interfaceAst) {
    super((InterfaceAstNode) interfaceAst);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitOutFilterAstNode(this);
  }

  @Override
  public <T> T accept(FilterAstNodeVisitor<T> visitor) {
    return visitor.visitOutFilterAstNode(this);
  }
}
