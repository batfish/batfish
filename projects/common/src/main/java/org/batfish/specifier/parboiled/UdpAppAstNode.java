package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

/** AST node for tcp application definition */
@ParametersAreNonnullByDefault
final class UdpAppAstNode extends PortAppAstNode {

  public UdpAppAstNode() {
    this(ImmutableList.of());
  }

  public UdpAppAstNode(List<SubRange> ports) {
    super(ports);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUdpAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitUdpAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UdpAppAstNode)) {
      return false;
    }
    UdpAppAstNode that = (UdpAppAstNode) o;
    return Objects.equals(_ports, that._ports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ports);
  }
}
