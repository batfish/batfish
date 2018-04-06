package org.batfish.datamodel.expr;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class MatchAcl extends AclLineExpr {
  private String _aclName;

  public MatchAcl(String aclName) {
    _aclName = aclName;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    List<IpAccessList> list =
        availableAcls
            .stream()
            .filter(a -> a.getName().equals(_aclName))
            .collect(Collectors.toList());
    return list.get(0).match(flow);
  }

  @Override
  public boolean exprEquals(Object o) {
    return _aclName == ((MatchAcl) o).getAclName();
  }

  @Override
  public int hashCode() {
    return _aclName.hashCode();
  }

  @Override
  public String toString() {
    return "PassAcl " + _aclName;
  }

  public String getAclName() {
    return _aclName;
  }
}
