package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.batfish.datamodel.HeaderSpace;

public class MatchHeaderspace extends AclLineMatchExpr {
  private static final long serialVersionUID = 1L;
  private final HeaderSpace _headerspace;

  public MatchHeaderspace(HeaderSpace headerspace) {
    _headerspace = headerspace;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchHeaderspace(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_headerspace, ((MatchHeaderspace) o)._headerspace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_headerspace);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("headerSpace", _headerspace).toString();
  }

  public HeaderSpace getHeaderspace() {
    return _headerspace;
  }
}
