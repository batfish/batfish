package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.Prefix;

final class PrefixAstNode implements IpSpaceAstNode {
  private final Prefix _prefix;

  PrefixAstNode(Prefix prefix) {
    _prefix = prefix;
  }

  PrefixAstNode(String prefix) {
    _prefix = Prefix.parse(prefix);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitPrefixAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitPrefixAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrefixAstNode)) {
      return false;
    }
    PrefixAstNode that = (PrefixAstNode) o;
    return Objects.equals(_prefix, that._prefix);
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_prefix);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("prefix", _prefix).toString();
  }
}
