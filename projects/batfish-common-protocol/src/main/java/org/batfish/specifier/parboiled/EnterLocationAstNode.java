package org.batfish.specifier.parboiled;

import java.util.Objects;

final class EnterLocationAstNode implements LocationAstNode {
  private final InterfaceLocationAstNode _interfaceLocationAst;

  EnterLocationAstNode(AstNode ast) {
    _interfaceLocationAst = (InterfaceLocationAstNode) ast;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitEnterLocationAstNode(this);
  }

  @Override
  public <T> T accept(LocationAstNodeVisitor<T> visitor) {
    return visitor.visitEnterLocationAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EnterLocationAstNode)) {
      return false;
    }
    EnterLocationAstNode that = (EnterLocationAstNode) o;
    return Objects.equals(_interfaceLocationAst, that._interfaceLocationAst);
  }

  public InterfaceLocationAstNode getInterfaceLocationAstNode() {
    return _interfaceLocationAst;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_interfaceLocationAst);
  }
}
