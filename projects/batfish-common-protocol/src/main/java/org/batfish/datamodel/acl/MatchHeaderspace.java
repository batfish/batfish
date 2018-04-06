package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import java.util.Objects;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;

public class MatchHeaderspace extends AclLineMatchExpr {
  private final HeaderSpace _headerspace;

  public MatchHeaderspace(HeaderSpace headerspace) {
    _headerspace = headerspace;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchHeaderspace(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return _headerspace.matches(flow);
  }

  @Override
  public boolean exprEquals(Object o) {
    return Objects.equals(_headerspace, ((MatchHeaderspace) o).getHeaderspace());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_headerspace);
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    helper.add("headerSpace", _headerspace);
    return helper.toString();
  }

  public HeaderSpace getHeaderspace() {
    return _headerspace;
  }
}
