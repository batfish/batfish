package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;

final class TypeNamedStructureAstNode implements NamedStructureAstNode {
  private final String _type;

  TypeNamedStructureAstNode(String type) {
    // canonicalize to the proper case
    _type =
        NamedStructurePropertySpecifier.JAVA_MAP.keySet().stream()
            .filter(p -> p.equalsIgnoreCase(type))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Invalid structure type " + type));
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTypeNamedStructureAstNode(this);
  }

  @Override
  public <T> T accept(NamedStructureAstNodeVisitor<T> visitor) {
    return visitor.visitTypeNamedStructureAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeNamedStructureAstNode)) {
      return false;
    }
    TypeNamedStructureAstNode that = (TypeNamedStructureAstNode) o;
    return Objects.equals(_type, that._type);
  }

  public String getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("type", _type).toString();
  }
}
