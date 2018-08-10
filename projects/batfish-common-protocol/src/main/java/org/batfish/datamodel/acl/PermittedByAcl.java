package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;

public class PermittedByAcl extends AclLineMatchExpr {
  private static final String PROP_ACL_NAME = "aclName";
  private static final String PROP_DEFAULT_ACCEPT = "defaultAccept";
  private static final long serialVersionUID = 1L;

  private final String _aclName;
  private final boolean _defaultAccept;

  public PermittedByAcl(String aclName, boolean defaultAccept) {
    this(aclName, defaultAccept, null);
  }

  public PermittedByAcl(String aclName) {
    this(aclName, false, null);
  }

  public PermittedByAcl(String aclName, String description) {
    this(aclName, false, description);
  }

  @JsonCreator
  public PermittedByAcl(
      @JsonProperty(PROP_ACL_NAME) String aclName,
      @JsonProperty(PROP_DEFAULT_ACCEPT) boolean defaultAccept,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _aclName = aclName;
    _defaultAccept = defaultAccept;
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
    return _aclName.equals(((PermittedByAcl) o)._aclName)
        && _defaultAccept == ((PermittedByAcl) o)._defaultAccept;
  }

  @JsonProperty(PROP_ACL_NAME)
  public String getAclName() {
    return _aclName;
  }

  @JsonProperty(PROP_DEFAULT_ACCEPT)
  public boolean getDefaultAccept() {
    return _defaultAccept;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName, _defaultAccept);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_ACL_NAME, _aclName)
        .add(PROP_DEFAULT_ACCEPT, _defaultAccept)
        .toString();
  }
}
