package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;

public class PermittedByAcl extends AclLineMatchExpr {
  private static final String PROP_ACL_NAME = "aclName";
  private static final long serialVersionUID = 1L;

  private final String _aclName;

  public PermittedByAcl(String aclName) {
    this(aclName, null);
  }

  @JsonCreator
  public PermittedByAcl(
      @JsonProperty(PROP_ACL_NAME) String aclName,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _aclName = aclName;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitPermittedByAcl(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return _aclName.compareTo(((PermittedByAcl) o)._aclName);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _aclName.equals(((PermittedByAcl) o)._aclName);
  }

  @JsonProperty(PROP_ACL_NAME)
  public String getAclName() {
    return _aclName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_ACL_NAME, _aclName).toString();
  }
}
