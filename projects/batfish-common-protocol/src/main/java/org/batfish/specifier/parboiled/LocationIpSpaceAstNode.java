package org.batfish.specifier.parboiled;

import java.util.Objects;

final class LocationIpSpaceAstNode implements IpSpaceAstNode {
  private final LocationAstNode _locationAst;

  LocationIpSpaceAstNode(AstNode locationAst) {
    _locationAst = (LocationAstNode) locationAst;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitLocationIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitLocationIpSpaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocationIpSpaceAstNode)) {
      return false;
    }
    LocationIpSpaceAstNode that = (LocationIpSpaceAstNode) o;
    return Objects.equals(_locationAst, that._locationAst);
  }

  public LocationAstNode getLocationAst() {
    return _locationAst;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_locationAst);
  }
}
