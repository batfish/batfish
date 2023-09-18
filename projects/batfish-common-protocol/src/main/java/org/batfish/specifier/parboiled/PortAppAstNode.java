package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

/** Common class for tcp and udp application definitions */
@ParametersAreNonnullByDefault
abstract class PortAppAstNode implements AppAstNode {

  protected final @Nonnull List<SubRange> _ports;

  public PortAppAstNode(List<SubRange> ports) {
    _ports = ports;
  }

  static PortAppAstNode createFrom(AstNode astNode, String port) {
    return createFrom(astNode, port, port);
  }

  static PortAppAstNode createFrom(AstNode astNode, String portFrom, String portTo) {
    int portFromInt = Integer.parseInt(portFrom);
    int portToInt = Integer.parseInt(portTo);

    checkArgument(portFromInt > 0 && portFromInt <= 65535, "Invalid port number: %s", portFrom);
    checkArgument(portToInt > 0 && portToInt <= 65535, "Invalid port number: %s", portTo);
    checkArgument(portFromInt <= portToInt, "Invalid port range: %s - %s", portFrom, portTo);

    List<SubRange> newList =
        ImmutableList.<SubRange>builder()
            .addAll(((PortAppAstNode) astNode).getPorts())
            .add(new SubRange(portFromInt, portToInt))
            .build();
    if (astNode instanceof TcpAppAstNode) {
      return new TcpAppAstNode(newList);
    } else if (astNode instanceof UdpAppAstNode) {
      return new UdpAppAstNode(newList);
    } else {
      throw new IllegalArgumentException(
          "Unknown PortAstNode class: " + astNode.getClass().getSimpleName());
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).omitNullValues().add("ports", _ports).toString();
  }

  public List<SubRange> getPorts() {
    return _ports;
  }
}
