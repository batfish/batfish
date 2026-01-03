package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

/** AST node for tcp application definition */
@ParametersAreNonnullByDefault
final class TcpAppAstNode extends PortAppAstNode {

  public TcpAppAstNode() {
    this(ImmutableList.of());
  }

  public TcpAppAstNode(List<SubRange> ports) {
    super(ports);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTcpAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitTcpAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TcpAppAstNode)) {
      return false;
    }
    TcpAppAstNode that = (TcpAppAstNode) o;
    return Objects.equals(_ports, that._ports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ports);
  }
}
