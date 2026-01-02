package org.batfish.datamodel.acl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

/**
 * An {@link AclLineMatchExpr} that evaluates to true when the flow matches a deny line of the
 * filter, or if it matches no line. The logical opposite of {@link PermittedByAcl}.
 */
public class DeniedByAcl extends AclLineMatchExpr {
  private static final String PROP_ACL_NAME = "aclName";

  private final String _aclName;

  public DeniedByAcl(String aclName) {
    this(aclName, (TraceElement) null);
  }

  public DeniedByAcl(String aclName, @Nullable String traceElement) {
    this(aclName, traceElement == null ? null : TraceElement.of(traceElement));
  }

  public DeniedByAcl(String aclName, @Nullable TraceElement traceElement) {
    super(traceElement);
    _aclName = aclName;
  }

  @JsonCreator
  private static DeniedByAcl jsonCreator(
      @JsonProperty(PROP_ACL_NAME) @Nullable String aclName,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    checkNotNull(aclName, "%s cannot be null", PROP_ACL_NAME);
    return new DeniedByAcl(aclName, traceElement);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitDeniedByAcl(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _aclName.equals(((DeniedByAcl) o)._aclName);
  }

  @JsonProperty(PROP_ACL_NAME)
  public String getAclName() {
    return _aclName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName, _traceElement);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_ACL_NAME, _aclName).toString();
  }
}
