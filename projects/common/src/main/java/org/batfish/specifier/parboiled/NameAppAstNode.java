package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.applications.NamedApplication;

final class NameAppAstNode implements AppAstNode {
  private final NamedApplication _namedApplication;

  NameAppAstNode(String name) {
    _namedApplication = NamedApplication.fromString(name);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitNameAppAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameAppAstNode)) {
      return false;
    }
    NameAppAstNode that = (NameAppAstNode) o;
    return Objects.equals(_namedApplication, that._namedApplication);
  }

  public NamedApplication getNamedApplication() {
    return _namedApplication;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_namedApplication);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("name", _namedApplication).toString();
  }
}
