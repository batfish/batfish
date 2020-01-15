package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AclLineMatchExpr implements Serializable {
  protected static final String PROP_DESCRIPTION = "description";

  protected final @Nullable String _description;

  @JsonCreator
  public AclLineMatchExpr(@JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    _description = description;
  }

  public abstract <R> R accept(GenericAclLineMatchExprVisitor<R> visitor);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(getClass() == o.getClass())) {
      return false;
    }
    return Objects.equals(_description, ((AclLineMatchExpr) o)._description) && exprEquals(o);
  }

  protected abstract boolean exprEquals(Object o);

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
