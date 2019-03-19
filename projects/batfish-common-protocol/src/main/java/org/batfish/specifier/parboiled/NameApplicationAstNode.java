package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.Protocol;

final class NameApplicationAstNode implements ApplicationAstNode {
  private final Protocol _protocol;

  NameApplicationAstNode(String name) {
    _protocol = Enum.valueOf(Protocol.class, name.toUpperCase());
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameApplicationAstNode(this);
  }

  @Override
  public <T> T accept(ApplicationAstNodeVisitor<T> visitor) {
    return visitor.visitNameApplicationAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameApplicationAstNode)) {
      return false;
    }
    NameApplicationAstNode that = (NameApplicationAstNode) o;
    return Objects.equals(_protocol, that._protocol);
  }

  public Protocol getProtocol() {
    return _protocol;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocol.ordinal());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("protocol", _protocol).toString();
  }
}
