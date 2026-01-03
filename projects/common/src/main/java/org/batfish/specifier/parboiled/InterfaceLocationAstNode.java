package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class InterfaceLocationAstNode implements LocationAstNode {
  private final @Nullable NodeAstNode _nodeAst;
  private final @Nullable InterfaceAstNode _interfaceAst;

  private InterfaceLocationAstNode(
      @Nullable NodeAstNode nodeAst, @Nullable InterfaceAstNode interfaceAst) {
    _nodeAst = nodeAst;
    _interfaceAst = interfaceAst;
  }

  static InterfaceLocationAstNode createFromInterface(AstNode interfaceAst) {
    checkArgument(
        interfaceAst instanceof InterfaceAstNode, "Unexpected argument: %s", interfaceAst);
    return new InterfaceLocationAstNode(null, (InterfaceAstNode) interfaceAst);
  }

  static InterfaceLocationAstNode createFromNode(AstNode nodeAst) {
    checkArgument(nodeAst instanceof NodeAstNode, "Unexpected argument: %s ", nodeAst);
    return new InterfaceLocationAstNode((NodeAstNode) nodeAst, null);
  }

  static InterfaceLocationAstNode createFromNode(String nodeName) {
    return new InterfaceLocationAstNode(new NameNodeAstNode(nodeName), null);
  }

  static InterfaceLocationAstNode createFromInterfaceWithNode(AstNode interfaceWithNode) {
    checkArgument(
        interfaceWithNode instanceof InterfaceWithNodeInterfaceAstNode,
        "Unexpected argument: %s",
        interfaceWithNode);

    InterfaceWithNodeInterfaceAstNode ast = (InterfaceWithNodeInterfaceAstNode) interfaceWithNode;
    return createFromInterfaceWithNode(ast.getNodeAstNode(), ast.getInterfaceAstNode());
  }

  static InterfaceLocationAstNode createFromInterfaceWithNode(
      NodeAstNode nodeAst, InterfaceAstNode interfaceAst) {
    return new InterfaceLocationAstNode(nodeAst, interfaceAst);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitInterfaceLocationAstNode(this);
  }

  @Override
  public <T> T accept(LocationAstNodeVisitor<T> visitor) {
    return visitor.visitInterfaceLocationAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceLocationAstNode)) {
      return false;
    }
    InterfaceLocationAstNode that = (InterfaceLocationAstNode) o;
    return Objects.equals(_nodeAst, that._nodeAst)
        && Objects.equals(_interfaceAst, that._interfaceAst);
  }

  public NodeAstNode getNodeAstNode() {
    return _nodeAst;
  }

  public InterfaceAstNode getInterfaceAstNode() {
    return _interfaceAst;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeAst, _interfaceAst);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("nodeAst", _nodeAst)
        .add("interfaceAst", _interfaceAst)
        .toString();
  }
}
