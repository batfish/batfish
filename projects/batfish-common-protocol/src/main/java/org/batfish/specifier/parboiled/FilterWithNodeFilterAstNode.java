package org.batfish.specifier.parboiled;

import java.util.Objects;

final class FilterWithNodeFilterAstNode implements FilterAstNode {
  private final NodeAstNode _nodeAstNode;
  private final FilterAstNode _filterAstNode;

  FilterWithNodeFilterAstNode(AstNode nodeAstNode, AstNode filterAstNode) {
    _nodeAstNode = (NodeAstNode) nodeAstNode;
    _filterAstNode = (FilterAstNode) filterAstNode;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitFilterWithNodeFilterAstNode(this);
  }

  @Override
  public <T> T accept(FilterAstNodeVisitor<T> visitor) {
    return visitor.visitFilterWithNodeFilterAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FilterWithNodeFilterAstNode)) {
      return false;
    }
    FilterWithNodeFilterAstNode that = (FilterWithNodeFilterAstNode) o;
    return Objects.equals(_nodeAstNode, that._nodeAstNode)
        && Objects.equals(_filterAstNode, that._filterAstNode);
  }

  public NodeAstNode getNodeAstNode() {
    return _nodeAstNode;
  }

  public FilterAstNode getFilterAstNode() {
    return _filterAstNode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeAstNode, _filterAstNode);
  }
}
