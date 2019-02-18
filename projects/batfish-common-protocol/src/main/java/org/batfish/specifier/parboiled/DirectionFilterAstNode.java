package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class DirectionFilterAstNode implements FilterAstNode {
  protected InterfaceAstNode _interfaceAst;

  static DirectionFilterAstNode create(String direction, AstNode interfaceAst) {
    if (direction.equalsIgnoreCase("@in") || direction.equalsIgnoreCase("inFilterOf")) {
      return new InFilterAstNode((InterfaceAstNode) interfaceAst);
    } else if (direction.equalsIgnoreCase("@out") || direction.equalsIgnoreCase("outFilterOf")) {
      return new OutFilterAstNode((InterfaceAstNode) interfaceAst);
    } else {
      throw new IllegalStateException("Unknown direction specifier for filters " + direction);
    }
  }

  public InterfaceAstNode getInterfaceAst() {
    return _interfaceAst;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DirectionFilterAstNode that = (DirectionFilterAstNode) o;
    return Objects.equals(getInterfaceAst(), that._interfaceAst);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), _interfaceAst);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("interfaceAst", _interfaceAst).toString();
  }
}
