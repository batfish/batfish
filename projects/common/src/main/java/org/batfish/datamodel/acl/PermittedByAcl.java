package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

public class PermittedByAcl extends AclLineMatchExpr {
  private static final String PROP_ACL_NAME = "aclName";

  private final String _aclName;

  public PermittedByAcl(String aclName) {
    this(aclName, (TraceElement) null);
  }

  public PermittedByAcl(String aclName, @Nullable String traceElement) {
    this(aclName, traceElement == null ? null : TraceElement.of(traceElement));
  }

  @JsonCreator
  public PermittedByAcl(
      @JsonProperty(PROP_ACL_NAME) String aclName,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    super(traceElement);
    _aclName = aclName;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitPermittedByAcl(this);
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
    return _aclName.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_ACL_NAME, _aclName).toString();
  }
}
