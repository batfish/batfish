package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class IcmpTypeAppAstNode implements AppAstNode {

  private final int _type;

  IcmpTypeAppAstNode(int type) {
    // TODO: stricter checking for valid ICMP types
    checkArgument(type <= 255, "Invalid ICMP type " + type);
    _type = type;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIcmpTypeAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitIcmpTypeAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpTypeAppAstNode)) {
      return false;
    }
    IcmpTypeAppAstNode that = (IcmpTypeAppAstNode) o;
    return Objects.equals(_type, that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_type);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).omitNullValues().add("type", _type).toString();
  }

  public int getType() {
    return _type;
  }
}
