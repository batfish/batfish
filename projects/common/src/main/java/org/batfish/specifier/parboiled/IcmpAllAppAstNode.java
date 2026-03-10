package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class IcmpAllAppAstNode implements AppAstNode {

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIcmpAllAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitIcmpAllAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IcmpAllAppAstNode)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }
}
