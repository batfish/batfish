package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;

public class PermittedByAcl extends AclLineMatchExpr {
  private final String _aclName;

  public PermittedByAcl(String aclName) {
    _aclName = aclName;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitPermittedByAcl(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return availableAcls.get(_aclName).filter(flow).getAction() == LineAction.ACCEPT;
  }

  @Override
  public boolean exprEquals(Object o) {
    return _aclName.equals(((PermittedByAcl) o).getAclName());
  }

  @Override
  public int hashCode() {
    return _aclName.hashCode();
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    helper.add("aclName", _aclName);
    return helper.toString();
  }

  public String getAclName() {
    return _aclName;
  }
}
