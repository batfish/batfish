package org.batfish.specifier.parboiled;

final class OutFilterAstNode extends DirectionFilterAstNode {

  OutFilterAstNode(InterfaceAstNode interfaceAst) {
    super(interfaceAst);
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
